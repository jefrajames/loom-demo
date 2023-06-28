package io.jefrajames.loomdemo.manytasks;

import java.security.SecureRandom;
import java.util.Random;
import java.util.concurrent.Semaphore;
import java.util.concurrent.atomic.LongAdder;
import java.util.stream.Stream;

import org.openjdk.jmh.infra.Blackhole;

/**
 * Simulates a database
 * 
 */
public class FakeDataBase {
    // Database connection pool size
    static final int POOL_SIZE = 50;

    private static final Random RANDOM = new SecureRandom();
    private static final Semaphore POOL = new Semaphore(POOL_SIZE);

    // A counter to keep track of the number of database operations
    private static LongAdder result = new LongAdder();

    // Simulate a database read
    public static Object get(String key) throws InterruptedException {
        // pretend to get a connection from a JDBC connection pool
        POOL.acquire();

        // pretend to talk to a remote server
        Thread.sleep(1 + RANDOM.nextInt(5));

        // pretend to get a result from the database
        Object readData = Long.toHexString(key.hashCode() + RANDOM.nextLong()).repeat(5 + RANDOM.nextInt(5));

        // Consume some CPU with semaphore
        Blackhole.consumeCPU(1000 + readData.hashCode() % 1000);

        // Put our thread back into the database.
        POOL.release();

        return readData;
    }

    public static long getResult() {
        return result.longValue();
    }

    // Simulate a database write
    public static void put(String key, Object value) throws InterruptedException {
        // pretend to marshal the update to the database
        long data = Stream.of(value.toString().toCharArray()).count();

        // pretend to get a connection from a JDBC connection pool
        POOL.acquire();

        // pretend to talk to a remote server
        Thread.sleep(2 + RANDOM.nextInt((int) (data % 3)));

        // pretend to care about the value
        result.add(key.hashCode());
        result.add(value.hashCode());
        result.add(data);

        // Put our thread back into the database.
        POOL.release();
    }
}
