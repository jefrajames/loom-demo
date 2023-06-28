
package io.jefrajames.loomdemo.basic;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.time.Duration;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.mutiny.Uni;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;

@Path("/reactive")
public class ReactiveResource {

    private static final int MEGA_BYTE = 1024 * 1024;

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private static final Runtime runtime = Runtime.getRuntime();

    // volatile guarantees visibility of change accross threads but not mutual exclusion
    // maxHeapSize may not be accurate with very high concurrency
    private static volatile long maxHeapSize;

    private final long sleepSeconds;

    @Inject
    public ReactiveResource(@ConfigProperty(name = "loom.sleep-seconds", defaultValue = "1") long sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    private static String threadKind() {
        return Thread.currentThread().isVirtual() ? "Virtual" : "Platform";
    }

    @GET
    @Path("/slow")
    public Uni<String> slow() {
        return Uni.createFrom().item(String.format("slow-%s", threadKind()))
                .onItem().delayIt().by(Duration.ofSeconds(sleepSeconds));
    }

    @GET
    @Path("/quick")
    public Uni<String> quick() {
        return Uni.createFrom().item(String.format("quick-%s", threadKind()));
    }

    @GET
    @Path("/memory")
    public Uni<String> memory() {

        String response = String.format("Memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                runtime.totalMemory() / MEGA_BYTE,
                (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE,
                memoryMXBean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                memoryMXBean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);

        return Uni.createFrom().item(response);
    }

    @GET
    @Path("/pinned/{timeout}")
    public Uni<String> pinned(@PathParam("timeout") Integer timeout) {

        synchronized (this) {
            return Uni.createFrom().item(String.format("pinned-%,dms-%s", timeout, threadKind()))
                    .onItem().delayIt().by(Duration.ofMillis(timeout));
        }
    }

    @GET
    @Path("/pid")
    public Uni<Long> pid() {
        return Uni.createFrom().item(ProcessHandle.current().pid());
    }

    @GET
    @Path("/heap")
    public Uni<String> heap() {
        var currentHeapSize = memoryMXBean.getHeapMemoryUsage().getUsed();
        if (currentHeapSize > maxHeapSize)
            maxHeapSize = currentHeapSize;

        return Uni.createFrom().item(String.format("%,d", currentHeapSize / MEGA_BYTE));
    }

    @GET
    @Path("/maxheap")
    public Uni<String> maxheap() {
        return Uni.createFrom().item(String.format("%,d", maxHeapSize / MEGA_BYTE));
    }

    @GET
    @Path("/gc")
    public Uni<Void> gc() {
        System.gc();
        return Uni.createFrom().voidItem();
    }

}
