package io.github.stscoundrel;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.Neo4jContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
public class ExporterIntegrationTest {
    private static final String adminPassword = "hunter2!";
    private static final String exporterPort = "9666";

    @Container
    private static final Neo4jContainer<?> neo4jContainer = new Neo4jContainer<>(DockerImageName.parse("neo4j:5.26.5"))
            .withEnv("NEO4J_PLUGINS", "[\"apoc-extended\"]")
            .withEnv("NEO4J_apoc_monitor_enabled", "true")
            .withAdminPassword(adminPassword);

    @BeforeAll
    public static void setup() throws Exception {
        // Start the exporter in another thread
        Thread exporterThread = new Thread(() -> {
            try {
                App.main(new String[]{
                        neo4jContainer.getBoltUrl(),
                        "neo4j",
                        adminPassword,
                        exporterPort
                });
            } catch (Exception e) {
                e.printStackTrace();
                fail("Exporter failed to start");
            }
        });
        exporterThread.setDaemon(true);
        exporterThread.start();

        waitForExporterReady();
    }

    @AfterAll
    public static void cleanup() {
        if (neo4jContainer != null) {
            neo4jContainer.stop();
        }

        App.shutdown();
    }

    private static void waitForExporterReady() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI metricsUri = URI.create("http://localhost:" + exporterPort + "/metrics");

        int retries = 30;
        for (int i = 0; i < retries; i++) {
            try {
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(metricsUri)
                        .timeout(Duration.ofSeconds(1))
                        .build();
                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                if (response.statusCode() == 200) {
                    System.out.println("Exporter is ready");
                    return;
                }
            } catch (Exception ignored) {
                // exporter not up yet
            }

            Thread.sleep(1000); // wait 1 second
        }

        fail("Exporter did not become ready in time");
    }

    @Test
    public void testMetricsEndpoint() throws Exception {
        HttpClient client = HttpClient.newHttpClient();
        URI metricsUri = URI.create("http://localhost:" + exporterPort + "/metrics");

        // Check initial metrics response.
        String metricsBody = fetchMetrics(client, metricsUri);
        assertTrue(metricsBody.contains("neo4j_monitor_ids_nodeIds 0.0"),
                "Expected initial node count to be 0.0");

        // Insert node into database.
        try (org.neo4j.driver.Driver driver = org.neo4j.driver.GraphDatabase.driver(
                neo4jContainer.getBoltUrl(),
                org.neo4j.driver.AuthTokens.basic("neo4j", adminPassword))) {
            try (org.neo4j.driver.Session session = driver.session()) {
                session.run("CREATE (n:Person {name: 'JavaDena'})");
            }
        }

        // Re-check metrics
        metricsBody = fetchMetrics(client, metricsUri);
        System.out.println("Updated metrics:\n" + metricsBody);
        assertTrue(metricsBody.contains("neo4j_monitor_ids_nodeIds 1.0"),
                "Expected node count to be 1.0 after insertion");
    }

    private String fetchMetrics(HttpClient client, URI uri) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(uri)
                .timeout(Duration.ofSeconds(5))
                .build();
        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
        assertEquals(200, response.statusCode(), "Metrics endpoint not reachable");
        return response.body();
    }
}
