
package io.jefrajames.loomdemo.basic;

import java.lang.management.ManagementFactory;
import java.lang.management.MemoryMXBean;
import java.lang.management.RuntimeMXBean;
import java.util.concurrent.TimeUnit;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;

@ApplicationScoped
@Path("/loom")
public class LoomResource {

    private static final int MEGA_BYTE = 1024 * 1024;

    private final long sleepSeconds;

    private static final MemoryMXBean memoryMXBean = ManagementFactory.getMemoryMXBean();

    private static RuntimeMXBean runtimeMXBean = ManagementFactory.getRuntimeMXBean();

    private static volatile long maxHeapSize;

    @Inject
    public LoomResource(@ConfigProperty(name = "loom.sleep-seconds", defaultValue = "1") long sleepSeconds) {
        this.sleepSeconds = sleepSeconds;
    }

    /**
     * Slow request
     *
     * @return done
     */
    @GET
    @Path("/slow")
    public String slowRequest() {
        try {
            TimeUnit.SECONDS.sleep(sleepSeconds);
        } catch (InterruptedException ignored) {
            // ignored for the sake of the example
        }
        return "slow";
    }

    /**
     * Quick request
     *
     * @return done immediately
     */
    @GET
    @Path("/quick")
    public String quickRequest() {
        return "quick";
    }

    @GET
    @Path("/thread")
    public String thread() {
        return Thread.currentThread().getName() + " (" + Thread.currentThread().isVirtual() + ")";
    }

   

    @GET
    @Path("/pinned")
    public String pinnedRequest() {
        synchronized (this) {
            slowRequest();
        }
        return "pinned";
    }

    @GET
    @Path("/pid")
    public String pidRequest() {
        // Get the pid of the current process
        return runtimeMXBean.getName().split("@")[0];
        // return "ps -o rss -p " + pid;
    }

    @GET
    @Path("/memory")
    public String memory() {

        MemoryMXBean mbean = ManagementFactory.getMemoryMXBean();

        return String.format("Memory total=%,d MB, used=%,d MB, heap=%,d MB, non heap=%,d MB%n",
                Runtime.getRuntime().totalMemory() / MEGA_BYTE,
                (Runtime.getRuntime().totalMemory() - Runtime.getRuntime().freeMemory()) / MEGA_BYTE,
                mbean.getHeapMemoryUsage().getUsed() / MEGA_BYTE,
                mbean.getNonHeapMemoryUsage().getUsed() / MEGA_BYTE);
    }

    @GET
    @Path("/heap")
    public String heap() {

        var currentHeapSize = memoryMXBean.getHeapMemoryUsage().getUsed();
        if (currentHeapSize > maxHeapSize)
            maxHeapSize=currentHeapSize;

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
