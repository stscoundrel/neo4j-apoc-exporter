package io.github.stscoundrel;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

public class App {
    private static Neo4jApocMetrics metrics;
    private static HTTPServer server;

    public static void main(String[] args) throws InterruptedException, IOException {

        JvmMetrics.builder().register();

        var config = Config.fromArgsOrEnv(args);

        metrics = new Neo4jApocMetrics(
                config.neo4jUri(),
                config.neo4jUser(),
                config.neo4jPassword()
        );
        metrics.registerAllMetrics();

        server = HTTPServer.builder()
                .port(config.exporterPort())
                .buildAndStart();

        System.out.println("Live metrics at http://localhost:" + server.getPort() + "/metrics");

        Thread.currentThread().join(); // keep running
    }

    public static void shutdown() {
        if (metrics != null) {
            metrics.close();
        }
        if (server != null) {
            server.close();
        }
    }
}
