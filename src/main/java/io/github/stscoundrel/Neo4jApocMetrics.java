package io.github.stscoundrel;

import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Map;

public class Neo4jApocMetrics {

    private final Driver driver;
    private final List<String> monitorTypes = List.of("ids", "store", "tx", "kernel");

    public Neo4jApocMetrics(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void registerAllMetrics() {
        for (String monitorType : monitorTypes) {
            registerMonitorMetrics(monitorType);
        }
    }

    private void registerMonitorMetrics(String monitorType) {
        try (Session session = driver.session()) {
            Record record = session.run("CALL apoc.monitor." + monitorType + "()").single();
            Map<String, Object> metrics = record.asMap();

            for (Map.Entry<String, Object> entry : metrics.entrySet()) {
                if (entry.getValue() instanceof Number) {
                    String key = entry.getKey();
                    String metricName = "neo4j_monitor_" + monitorType + "_" + key;

                    GaugeWithCallback.builder()
                            .name(metricName)
                            .help("Metric " + key + " from apoc.monitor." + monitorType)
                            .callback(callback -> {
                                try (Session innerSession = driver.session()) {
                                    Record updatedRecord = innerSession
                                            .run("CALL apoc.monitor." + monitorType + "()").single();
                                    Object updatedValue = updatedRecord.get(key).asObject();

                                    if (updatedValue instanceof Number) {
                                        callback.call(((Number) updatedValue).doubleValue());
                                    }
                                } catch (Exception e) {
                                    System.err.println("Failed to fetch live metric for " + metricName);
                                    e.printStackTrace();
                                }
                            })
                            .register();

                    System.out.println("Registered: " + metricName);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to initialize metrics for: " + monitorType);
            e.printStackTrace();
        }
    }

    public void close() {
        driver.close();
    }
}
