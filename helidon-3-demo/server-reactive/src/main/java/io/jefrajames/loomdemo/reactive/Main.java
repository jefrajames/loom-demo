package io.jefrajames.loomdemo.reactive;

import io.helidon.common.LogConfig;
import io.helidon.config.Config;
import io.helidon.webserver.Routing;
import io.helidon.webserver.WebServer;

public class Main {
    public static void main(String[] args) {
        LogConfig.configureRuntime();
        Config config = Config.create();

        WebServer.builder()
                .config(config.get("server"))
                .routing(Routing.builder()
                                 .register("/reactive", new ReactiveService(config)))
                .build()
                .start()
                .await();
    }
}
