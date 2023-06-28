package io.jefrajames.loomdemo.maxthread;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;

/**
 * 
 * Illustrates how many Platform Threads can be run.
 * 
 */
public class MaxPThreads {

    private static final int MEGA_BYTE = 1024 * 1024;
    private static final int THREAD_COUNT = 1_000_000;
    private static final int LOG_INTERVAL = 1_000;
    private static final int LOG_THRESHOLD = 4_000;

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

        List<Thread> threads = new ArrayList<>();
        CountDownLatch hold = new CountDownLatch(1);

        while (threads.size() < THREAD_COUNT) {
            CountDownLatch started = new CountDownLatch(1);
            try {
                Thread thread = Thread.ofPlatform().start(() -> {
                    try {
                        started.countDown();
                        hold.await();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                });

                threads.add(thread);

                if (threads.size() >= LOG_THRESHOLD && (threads.size() % LOG_INTERVAL) == 0) {
                    printMemory(threads);
                }
            } catch (Throwable e) {
                // thread.size=4067 on MacOS (by default kern.num_taskthreads=4096)
                System.err.println("Got Throwable : " + e.getMessage());
                System.err.println("threads.size=" + threads.size());
                printMemory(threads);
                System.exit(1);
            
            }
        } // Eof thread processing

        // Never executed!
        hold.countDown();
        for (Thread thread : threads) {
            thread.join();
        }
    }
}
