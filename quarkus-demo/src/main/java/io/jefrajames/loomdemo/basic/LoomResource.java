
package io.jefrajames.loomdemo.basic;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import io.smallrye.common.annotation.RunOnVirtualThread;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;

@RunOnVirtualThread
@Path("/loom")
@Produces("text/plain")
public class LoomResource {

    private static final int MEGA_BYTE = 1024 * 1024;

    private static volatile long maxHeapSize;

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private static final Runtime runtime = Runtime.getRuntime();

    private final long sleepSeconds;

    @Inject
    public LoomResource(@ConfigProperty(name = "loom.sleep-seconds", defaultValue = "1") long sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    private static String threadKind() {
        return Thread.currentThread().isVirtual() ? "Virtual" : "Platform";
    }


    @GET
    @Path("/slow")
    public String slow() throws InterruptedException {
        TimeUnit.SECONDS.sleep(sleepSeconds);
        return String.format("slow-%s", threadKind());
    }


    @GET
    @Path("/quick")
    public String quickRequest() {
        return String.format("quick-%s", threadKind());

    }

    @GET
    @Path("/memory")
    public String memory() {

        return String.format("Memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                runtime.totalMemory() / MEGA_BYTE,
                (runtime.totalMemory() - runtime.freeMemory()) / MEGA_BYTE,
                memoryMXBean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                memoryMXBean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);
    }

    @GET
    @Path("/pinned/{timeout}")
    public String pinned(@PathParam("timeout") int timeout) throws InterruptedException {
        synchronized (this) {
            TimeUnit.MILLISECONDS.sleep(timeout);
        }
        return String.format("pinned-%,dms-%s", timeout, threadKind());
    }

    @GET
    @Path("/pid")
    public long pid() {
        return ProcessHandle.current().pid();
    }

    @GET
    @Path("/heap")
    public String heap() {

        var currentHeapSize = memoryMXBean.getHeapMemoryUsage().getUsed();
        if (currentHeapSize > maxHeapSize)
            maxHeapSize = currentHeapSize;

        return String.format("%,d", currentHeapSize / MEGA_BYTE);
    }

    @GET
    @Path("/maxheap")
    public String maxheap() {
        return String.format("%,d", maxHeapSize / MEGA_BYTE);
    }

    @GET
    @Path("/gc")
    public void gc() {
        System.gc();
    }

}
