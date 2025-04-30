package io.github.stscoundrel;

import io.prometheus.metrics.exporter.httpserver.HTTPServer;
import io.prometheus.metrics.instrumentation.jvm.JvmMetrics;

import java.io.IOException;

public class App {
    public static void main(String[] args) throws InterruptedException, IOException {

        JvmMetrics.builder().register();

        Neo4jApocMetrics metrics = new Neo4jApocMetrics(
                "bolt://localhost:7687", // TODO: from ENV
                "neo4j",// TODO: from ENV
                "password"// TODO: from ENV
        );
        metrics.registerAllMetrics();

        HTTPServer server = HTTPServer.builder()
                .port(9400) // TODO: from Env
                .buildAndStart();

        System.out.println("Live metrics at http://localhost:" + server.getPort() + "/metrics");

        Thread.currentThread().join(); // keep running
    }
}
