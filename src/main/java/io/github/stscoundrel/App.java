package io.github.stscoundrel;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {

        JvmMetrics.builder().register();

        Config config = Config.fromEnv();

        Neo4jApocMetrics metrics = new Neo4jApocMetrics(
                config.neo4jUri(),
                config.neo4jUser(),
                config.neo4jPassword()
        );
        metrics.registerAllMetrics();

        HTTPServer server = HTTPServer.builder()
                .port(config.exporterPort())
                .buildAndStart();

        System.out.println("Live metrics at http://localhost:" + server.getPort() + "/metrics");

        Thread.currentThread().join(); // keep running
    }
}
