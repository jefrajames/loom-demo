package io.jefrajames.loomdemo.deepstack;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.LongAdder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Callled by MaxDeepVThreads and MaxDeepPThreads.
 * 
 * Sequentially launch THREAD_COUNT threads running a DNA mutation algorithm.
 * 
 * A hard limit is reached with Platform Threads. 
 * On MacOS, it is capped by the kernel variable kern.num_taskthreads (4096).
 * On Linux, it is limited by the memory size of the stack.
 * 
 * There is no hard limit with Virtual Threads. 
 * The processing is stopped when it is considered too slow.
 * Concretly when it takes more than SLOWNESS_THRESHOLD msec
 * to create a Virtual Thread.
 * 
 * 
 */
public class MaxThreads {

    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int THREAD_COUNT = 100_000;
    private static final int LOG_INTERVALL = 10_000;
    private static final int SLOWNESS_DETECTION = 1_000;
    private static final int SLOWNESS_THRESHOLD = 2_000;
    private static final Pattern WORKER_PATTERN = Pattern.compile("worker-[\\d?]");

    private static final LongAdder result = new LongAdder();

    private static void printMemory(List<Thread> threads) {
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

        System.err.printf("Thread count %,d: memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                threads.size(),
                Runtime.getRuntime().totalMemory() / MEGA_BYTE,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE,
                mbean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                mbean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);

    }

    public static void findMax(int depth, boolean virtual) throws Exception {
        DnaStack.warmup();

        Map<String, AtomicInteger> cThreads = new ConcurrentHashMap<>();

        DnaStack dna = new DnaStack();
        CountDownLatch hold = new CountDownLatch(1);
        List<Thread> threads = new ArrayList<>();

        // Create threads until we get an OutOfMemoryError with Platform Threads
        // Or a high GC pressure with Virtual Threads
        while (threads.size() < THREAD_COUNT) {
            long start = System.nanoTime();
            CountDownLatch latch = new CountDownLatch(1);
            ThreadFactory tf;

            if (virtual)
                tf = Thread.ofVirtual().factory();
            else
                tf = Thread.ofPlatform().factory();

            // Create a thread (Virtual or Platform) running the DNA mutation
            Thread t = tf.newThread(() -> {
                if (virtual) {
                    // System.err.println("Running with thread " + Thread.currentThread());
                    String name = Thread.currentThread().toString();
                    Matcher workerMatcher = WORKER_PATTERN.matcher(name);
                    if ( workerMatcher.find() ) {
                        var cpt = cThreads.get(workerMatcher.group());
                        if (cpt == null) {
                            cpt = new AtomicInteger(1);
                            cThreads.put(workerMatcher.group(), cpt);
                        } else {
                            cpt.incrementAndGet();
                        }
                    }
                }
                String d = dna.next("", s -> {
                    if (s.length() < depth)
                        return false; // Not finished yet
                    latch.countDown(); // Finished!
                    try {
                        hold.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    return true;
                },
                        l -> l.get(0));
                result.add(d.hashCode());
            });

            t.start();
            threads.add(t);

            // Print some stats
            if ((threads.size() % LOG_INTERVALL) == 0) {
                printMemory(threads);
            }

            // Wait for the thread to terminate its task
            latch.await();

            // How long to start the last thread?
            long wait = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - start);
            if (wait > SLOWNESS_DETECTION) {
                System.err.printf("Warning slow thread creation %,dms (thread count=%,d)%n", wait, threads.size());
                if (wait >= SLOWNESS_THRESHOLD) {
                    System.err.printf("Thread creation too slow %,d threads created%n", threads.size());
                    break;
                }
            }
        }

        // Release all threads
        hold.countDown();

        if (virtual) {
            // Summary of Carrier Threads use
            System.out.println("Available cores: " + Runtime.getRuntime().availableProcessors());
            System.out.println("Carrier Threads: " + cThreads.size());
            cThreads.forEach((k, v) -> System.out.printf("Platform thread %s called %,d times%n", k, v.intValue()));
        }

        // Print the result
        System.err.println("Test done!");
        System.err.printf("Before exiting, please check the process RSS by running ps -o rss -p %d%n",
                ProcessHandle.current().pid());
        System.err.println("Press enter to exit");
        System.in.read();

        // Joining threads should be done but is too slow for the demo ...

    }
}
