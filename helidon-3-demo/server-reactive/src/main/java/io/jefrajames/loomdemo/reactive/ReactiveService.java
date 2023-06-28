package io.jefrajames.loomdemo.reactive;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.helidon.common.configurable.ScheduledThreadPoolSupplier;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.ServerRequest;
import io.helidon.webserver.ServerResponse;
import io.helidon.webserver.Service;

class ReactiveService implements Service {
    private final Integer sleepSeconds;
    private final ScheduledExecutorService scheduledExecutorService;

    private static MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private static final Runtime runtime = Runtime.getRuntime();

    private static final int MEGA_BYTE = 1024 * 1024;

    // volatile guarantees visibility of change accross threads but not mutual exclusion
    // maxHeapSize may not be accurate with very high concurrency
    private static volatile long maxHeapSize;

    ReactiveService(Config config) {
        this.sleepSeconds = config.get("loom.sleep-seconds").asInt().orElse(1);
        this.scheduledExecutorService = ScheduledThreadPoolSupplier.builder()
                .corePoolSize(10)
                .prestart(true)
                .build()
                .get();
    }

    @Override
    public void update(Routing.Rules rules) {
        rules.get("/slow", this::slow)
                .get("/quick", this::quick)
                .get("/memory", this::memory)
                .get("/pid", this::pid)
                .get("/heap", this::heap)
                .get("/maxheap", this::maxheap)
                .get("/gc", this::gc)
                ;
    }

    private static String threadKind() {
        return Thread.currentThread().isVirtual() ? "Virtual" : "Platform";
    }

    private void quick(ServerRequest req, ServerResponse res) {
        res.send(String.format("quick-%s", threadKind()));
    }

    private void slow(ServerRequest req, ServerResponse res) {
        scheduledExecutorService.schedule(() -> res.send(String.format("slow-%s", threadKind())), sleepSeconds, TimeUnit.SECONDS);
    }

    private void pid(ServerRequest req, ServerResponse res) {
        res.send(ProcessHandle.current().toString());
    }

    private void memory(ServerRequest req, ServerResponse res) {
        res.send(String.format("Memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                runtime.totalMemory() / MEGA_BYTE,
                (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE,
                memoryMXBean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                memoryMXBean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE));
    }

    private void heap(ServerRequest req, ServerResponse res) {
        res.send(String.format("%,d", memoryMXBean.getHeapMemoryUsage().getUsed() / MEGA_BYTE));
    }
    

    public void maxheap(ServerRequest req, ServerResponse res) {
        var currentHeapSize = memoryMXBean.getHeapMemoryUsage().getUsed();
        if (currentHeapSize > maxHeapSize)
            maxHeapSize = currentHeapSize;

        res.send(String.format("%,d", maxHeapSize / MEGA_BYTE));
    }

    
    public void gc(ServerRequest req, ServerResponse res) {
        System.gc();
        res.send("GC done");
    }

}
