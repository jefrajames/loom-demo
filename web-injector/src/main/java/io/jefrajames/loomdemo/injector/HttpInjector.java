package io.jefrajames.loomdemo.injector;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpClient.Builder;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.net.http.HttpTimeoutException;
import java.time.Duration;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import io.helidon.config.Config;

/**
 * 
 * Custom multithreaded REST injector based on Java HTTP client
 * 
 */
public class HttpInjector {

    private static final AtomicInteger SUCCESS = new AtomicInteger();
    private static final AtomicInteger TIMEOUT = new AtomicInteger();
    private static final AtomicInteger ERROR = new AtomicInteger();
    private static String RESPONSE;

    private static final int WARMUP_COUNT = 1_000;

    private static String get(HttpClient client, String uri, int readTimeout) {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(uri))
                .GET()
                .timeout(Duration.ofMillis(readTimeout))
                .build();

        HttpResponse<String> response;
        try {
            response = client.send(request, BodyHandlers.ofString());
            if (response.statusCode() != 200) {
                ERROR.incrementAndGet();
                return null;
            }
            SUCCESS.incrementAndGet();
            RESPONSE = response.body();
            return response.body();
        } catch (HttpTimeoutException e) {
            TIMEOUT.incrementAndGet();
            return null;
        } catch (IOException | InterruptedException e) {
            ERROR.incrementAndGet();
            return null;
        }

    }

    private static void warmup(Builder builder, String uri, int readTimeout) throws Exception {
        System.out.printf("Warming up server with %,d requests ...", WARMUP_COUNT);
        HttpClient client = builder.build();
        for (int j = 0; j < WARMUP_COUNT; j++) {
            get(client, uri, readTimeout);
        }
        System.out.println(" done!");
    }

    private static double computeAverageThroughput(int repeats, int threadCount, long start) {
        long end = System.nanoTime();
        long elapsedTime = end - start;
        double elapsedTimeInSecond = (double) elapsedTime / 1_000_000_000;
        double throughput = (repeats * threadCount) / elapsedTimeInSecond;
        return throughput;
    }

    public static void main(String[] args) throws Exception {

        // Load properties
        Config config = Config.create().get("loom");
        int threadCount = config.get("threads").asInt().orElse(10);
        int repeats = config.get("repeats").asInt().orElse(60_000);
        long monitorPeriodSeconds = config.get("monitor.period-seconds").asLong().orElse(1L);
        boolean monitorPrintHeader = config.get("monitor.print-header").asBoolean().orElse(true);
        int monitorPrintHeaderEvery = config.get("monitor.print-header-lines").asInt().orElse(10);
        boolean benchActive = config.get("bench.active").asBoolean().orElse(false);
        boolean warmup = config.get("bench.warmup").asBoolean().orElse(false);
        int connectTimeout = config.get("client.connect.connect-timeout-millis").asInt().orElse(1000);
        int readTimeout = config.get("client.read-timeout-millis").asInt().orElse(2000);
        String uri = config.get("client.uri").asString().orElse("http://localhost:8080/loom/quick");

        Builder builder = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(connectTimeout))
                .version(HttpClient.Version.HTTP_1_1);

        if (benchActive && warmup)
            warmup(builder, uri, readTimeout);

        long start = System.nanoTime();

        Thread[] threads = new Thread[threadCount];

        for (int i = 0; i < threads.length; i++) {
            threads[i] = new Thread(() -> {
                HttpClient client = builder.build();
                for (int j = 0; j < repeats; j++) {
                    get(client, uri, readTimeout);
                }
            });
        }

        // start monitoring
        Monitor monitor = new Monitor(uri, monitorPeriodSeconds, monitorPrintHeader, monitorPrintHeaderEvery);
        Thread monitorThread = new Thread(monitor);
        monitorThread.setDaemon(true);
        monitorThread.start();

        for (Thread thread : threads) {
            thread.start();
        }

        for (Thread thread : threads) {
            thread.join();
        }

        monitor.finish();

        // Print throughput
        if (benchActive) {
            double throughput = computeAverageThroughput(repeats, threadCount, start);
            System.out.printf("Average throughput is %,9.2f Req/s%n", throughput);
        }

    }

    private static class Monitor implements Runnable {
        private final String uri;
        private final long sleepSeconds;
        private final boolean printHeader;
        private final int printHeaderEvery;
        private volatile boolean finish;
        private final CountDownLatch finishLatch = new CountDownLatch(1);
        private int lastCount = 0;
        private long lastRun;
        private int printCounter;

        private Monitor(String uri, long sleepSeconds, boolean printHeader, int printHeaderEvery) {
            this.uri = uri;
            this.sleepSeconds = sleepSeconds;
            this.printHeader = printHeader;
            this.printHeaderEvery = printHeaderEvery;
        }

        @Override
        public void run() {
            System.out.println("Monitoring requests to " + uri);

            if (printHeader) {
                header();
            }

            lastRun = System.nanoTime();
            while (!finish) {
                try {
                    TimeUnit.SECONDS.sleep(sleepSeconds);
                } catch (InterruptedException e) {
                    // we were interrupted, no need to move forward
                    return;
                }
                if (!finish) {
                    print();
                }
            }
            finishLatch.countDown();
        }

        private void header() {
            if (printHeader) {
                System.out.println("    Req/s    Success  Timeout  Error   Response");
            }
        }

        private void print() {
            int currentCount = SUCCESS.get();
            long now = System.nanoTime();

            int countLambda = currentCount - lastCount;
            long timeLambda = now - lastRun;
            long timeLambdaSeconds = TimeUnit.NANOSECONDS.toSeconds(timeLambda);
            double perSecond = 0;
            if (timeLambdaSeconds != 0) {
                perSecond = (double) countLambda / timeLambdaSeconds;
            }

            System.out.printf("%,9.2f %10d %8d %6d %10s%n", perSecond, SUCCESS.get(), TIMEOUT.get(), ERROR.get(),
                    RESPONSE);
            lastCount = currentCount;
            lastRun = now;
            printCounter++;
            if (printCounter % printHeaderEvery == 0) {
                header();
            }
        }

        private void finish() throws InterruptedException {
            finish = true;
            finishLatch.await();
            print();
            System.out.println("Finished");
        }
    }
}
