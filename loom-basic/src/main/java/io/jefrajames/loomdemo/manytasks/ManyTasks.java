package io.jefrajames.loomdemo.manytasks;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.eclipse.jetty.util.thread.QueuedThreadPool;
import org.openjdk.jmh.infra.Blackhole;

/**
 * Run a number of tasks simulating database access in two modes:
 * with pooled Platform Threads and with Virtual Threads.
 */
public class ManyTasks {

    private static final int WARMUP_SIZE = 1_000;
    private static final int TASK_COUNT = 100_000;
    private static final int THREAD_POOL_SIZE = 100;
    private static final int MEGA_BYTE = 1024 * 1024;

    private static final Random RANDOM = new SecureRandom();

    // A task that simulates db access (2 reads, 1 write) and then signals completion
    static Runnable newTask(CountDownLatch latch) {
        long id = RANDOM.nextLong();
        String task = Integer.toHexString(RANDOM.nextInt());
        return () -> {
            try {
                // pretend to authenticate the user
                Object userdata = FakeDataBase.get(Long.toString(id));

                // small chance auth fails.
                if (userdata.toString().startsWith("6666"))
                    return;

                // pretend to get some app data
                Object data = FakeDataBase.get(task);

                // pretend to process the data
                Blackhole.consumeCPU(1000 + data.hashCode() % 1000);

                // pretend to mutate some app data
                FakeDataBase.put(task, Long.toHexString(data.hashCode() + id).repeat(1 + Math.abs((int) (id % 10))));
            } catch (InterruptedException e) {
                e.printStackTrace();
            } finally {
                // signal completion
                latch.countDown();
            }
        };
    }

    // Warmup the JVM
    private static void warmup(int tasks) throws InterruptedException {

        System.err.printf("Warming up with %,d tasks...", tasks);
        CountDownLatch warmup = new CountDownLatch(tasks * 2);
        for (int i = 0; i < tasks; i++) {
            Thread.ofPlatform().start(newTask(warmup));
            Thread.ofPlatform().start(newTask(warmup));
        }
        warmup.await();
        System.err.println(" done!");
    }

    // Run tasks with a thread pool
    private static long testThreadPool(int tasks) throws Exception {

        CountDownLatch latch = new CountDownLatch(tasks);
        // Create a thread pool
        QueuedThreadPool threadPool = new QueuedThreadPool(THREAD_POOL_SIZE, THREAD_POOL_SIZE, -1, 0, null, null);
        threadPool.start();

        try {
            // start the clock
            long started = System.nanoTime();
            // run tasks with a thread pool
            for (int i = 0; i < tasks; i++)
                threadPool.execute(newTask(latch));
            // wait for completion
            latch.await();
            //
            return System.nanoTime() - started;
        } finally {
            //
            threadPool.stop();
        }
    }

    // Run tasks with virtual threads
    private static long testVThreads(int tasks) throws Exception {

        CountDownLatch latch = new CountDownLatch(tasks);

        long started = System.nanoTime();
        // run tasks with virtual threads
        for (int i = 0; i < tasks; i++)
            Thread.ofVirtual().start(newTask(latch));
        // wait for completion
        latch.await();
        return System.nanoTime() - started;
    }

    private static void printMemory(String msg) {
        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();
        System.err.printf("%s, memory: total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                msg,
                Runtime.getRuntime().totalMemory() / MEGA_BYTE,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE,
                mbean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                mbean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);

    }

    // Java option: --enable-preview
    public static void main(String... args) throws Exception {

        System.err.printf("Running %,d tasks with database connexion pool size %d%n", TASK_COUNT,
                FakeDataBase.POOL_SIZE);


        warmup(WARMUP_SIZE);

        System.gc();

        System.err.println("First round with Pooled Platform Threads");
        printMemory("Before testing");
        System.err.printf("%,d tasks run in %,d ms%n", TASK_COUNT,
                TimeUnit.NANOSECONDS.toMillis(testThreadPool(TASK_COUNT)));
        printMemory("After testing");

        System.gc();

        System.err.println("\n\nSecond round with Virtual Threads");
        printMemory("Before testing");
        System.err.printf("%,d tasks run in %,d ms%n", TASK_COUNT,
                TimeUnit.NANOSECONDS.toMillis(testVThreads(TASK_COUNT)));
        printMemory("After testing");

        System.err.println("All done!");
        // System.err.println(FakeDataBase.getResult());
    }

}
