package io.jefrajames.loomdemo.maxthread;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Illustrates how long it takes to start (and join) 1 million Virtual
 * Threads
 * and how many carrier Platform Threads are used under the hood
 */
public class MaxVThreads {

    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int THREAD_COUNT = 1_000_000;
    private static final int LOG_INTERVAL = 100_000;
    private static final Pattern WORKER_PATTERN = Pattern.compile("worker-[\\d?]");

    // Virtual Thread processing
    private static void process(Map<String, AtomicInteger> pThreads) {
        String threadName = Thread.currentThread().toString();
        Matcher workerMatcher = WORKER_PATTERN.matcher(threadName);
        if ( workerMatcher.find()) {
            var cpt = pThreads.get(workerMatcher.group());
            if (cpt == null) {
                cpt = new AtomicInteger(1);
                pThreads.put(workerMatcher.group(), cpt);
            } else {
                cpt.incrementAndGet();
            }
        }
    }

    private static void printMemory(List<Thread> threads) {
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

        System.err.printf("Thread count %,d: memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                threads.size(),
                Runtime.getRuntime().totalMemory() / MEGA_BYTE,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE,
                mbean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                mbean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);

    }

    // Java options: --enable-preview
    public static void main(String... args) throws Exception {

        Map<String, AtomicInteger> pThreads = new ConcurrentHashMap<>();

        List<Thread> threads = new ArrayList<>(THREAD_COUNT);
        CountDownLatch hold = new CountDownLatch(1);

        System.err.println(String.format("Starting %,d Virtual Threads in parallel...", THREAD_COUNT));
        Instant begin = Instant.now();
        while (threads.size() < THREAD_COUNT) {
            CountDownLatch started = new CountDownLatch(1);
            Thread thread = Thread.ofVirtual().start(() -> {
                // Virtual thread processing
                process(pThreads);
                started.countDown(); // decrement started
                try {
                    hold.await(); // block until hold count equals 0
                } catch (InterruptedException ignore) {
                }

            });

            // Back to main thread
            threads.add(thread);
            started.await(); // block until started count equals 0: ensures the thread is started
            if ((threads.size() % LOG_INTERVAL) == 0) {
                printMemory(threads);
            }
        }
        Instant end = Instant.now();
        System.err.println(
                String.format("%,d Virtual Threads started in %,d ms", THREAD_COUNT,
                        Duration.between(begin, end).toMillis()));

        // Ensures that all threads are running in //
        hold.countDown(); // decrement hold => unblock all awaiting threads

        // Start summary
        System.out.println("Available cores: " + Runtime.getRuntime().availableProcessors());
        System.out.println("Carrier Threads: " + pThreads.size());
        pThreads.forEach((k, v) -> System.out.printf("Platform thread %s called %,d times%n", k, v.intValue()));

        begin = Instant.now();
        System.err.println("Joining all Virtual Threads...");
        for (Thread thread : threads) {
            thread.join();
        }
        end = Instant.now();
        System.err.println(
            String.format("%,d Virtual Threads joined in %,d ms", THREAD_COUNT,
                    Duration.between(begin, end).toMillis()));
    }

}
