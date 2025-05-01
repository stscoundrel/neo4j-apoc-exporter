package io.github.stscoundrel;

import io.prometheus.metrics.core.metrics.GaugeWithCallback;
import org.neo4j.driver.*;
import org.neo4j.driver.Record;

import java.util.List;
import java.util.Map;

public class Neo4jApocMetrics {

    private final Driver driver;
    private final List<String> monitorTypes = List.of("ids", "store", "tx");

    public Neo4jApocMetrics(String uri, String username, String password) {
        this.driver = GraphDatabase.driver(uri, AuthTokens.basic(username, password));
    }

    public void registerAllMetrics() {
        for (String monitorType : monitorTypes) {
            registerMonitorMetrics(monitorType);
        }
    }

    private void registerMonitorMetrics(String monitorType) {
        Map<String, String> allowedMetrics = MetricsRegistry.ALLOWED_METRICS.get(monitorType);
        if (allowedMetrics == null) {
            System.err.println("No metric mappings defined for monitor type: " + monitorType);
            return;
        }

        try (Session session = driver.session()) {
            Record record = session.run("CALL apoc.monitor." + monitorType + "()").single();
            Map<String, Object> apocData = record.asMap();

            for (Map.Entry<String, String> allowedEntry : allowedMetrics.entrySet()) {
                String apocKey = allowedEntry.getKey();
                String metricName = allowedEntry.getValue();

                if (!apocData.containsKey(apocKey)) {
                    System.err.println("Missing expected APOC key: " + apocKey + " in monitor: " + monitorType);
                    continue;
                }

                Object value = apocData.get(apocKey);
                if (value instanceof Number) {
                    GaugeWithCallback.builder()
                            .name(metricName)
                            .help("Metric " + apocKey + " from apoc.monitor." + monitorType)
                            .callback(callback -> {
                                try (Session innerSession = driver.session()) {
                                    Record updated = innerSession
                                            .run("CALL apoc.monitor." + monitorType + "()").single();
                                    Object newVal = updated.get(apocKey).asObject();
                                    if (newVal instanceof Number) {
                                        callback.call(((Number) newVal).doubleValue());
                                    }
                                } catch (Exception e) {
                                    System.err.println("Error updating metric " + metricName);
                                    e.printStackTrace();
                                }
                            })
                            .register();

                    System.out.println("Registered: " + metricName);
                } else {
                    System.err.println("Expected numeric value for " + apocKey + ", got: " + value);
                }
            }
        } catch (Exception e) {
            System.err.println("Failed to register metrics for monitor type: " + monitorType);
            e.printStackTrace();
        }
    }

    public void close() {
        driver.close();
    }
}
