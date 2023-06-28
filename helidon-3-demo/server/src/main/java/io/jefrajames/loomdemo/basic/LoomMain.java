package io.jefrajames.loomdemo.basic;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import io.helidon.microprofile.cdi.Main;
import io.helidon.microprofile.server.Server;

public class LoomMain {
    public static void main(String[] args) {
        Config config = ConfigProvider.getConfig();
        boolean useVirtual = config.getValue("server.executor-service.virtual-threads", Boolean.class);


        if (useVirtual) {
            System.out.println("Using virtual threads");
            ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor();
            Server.builder()
                    .defaultExecutorServiceSupplier(() -> executor)
                    .build()
                    .start();
        } else {
            System.out.println("Using executor service");
            Main.main(args);
        }
    }
}
