package io.github.stscoundrel;

import java.util.Map;

public class MetricsRegistry {
    public static final Map<String, Map<String, String>> ALLOWED_METRICS = Map.of(
            "ids", Map.of(
                    "nodeIds", "neo4j_monitor_ids_nodeIds",
                    "relIds", "neo4j_monitor_ids_relIds",
                    "propIds", "neo4j_monitor_ids_propIds",
                    "relTypeIds", "neo4j_monitor_ids_relTypeIds"
            ),
            "store", Map.of(
                    "logSize", "neo4j_monitor_store_logSize",
                    "stringStoreSize", "neo4j_monitor_store_stringStoreSize",
                    "arrayStoreSize", "neo4j_monitor_store_arrayStoreSize",
                    "relStoreSize", "neo4j_monitor_store_relStoreSize",
                    "propStoreSize", "neo4j_monitor_store_propStoreSize",
                    "totalStoreSize", "neo4j_monitor_store_totalStoreSize",
                    "nodeStoreSize", "neo4j_monitor_store_nodeStoreSize"
            ),
            "tx", Map.of(
                    "rolledBackTx", "neo4j_monitor_tx_rolledBackTx",
                    "peakTx", "neo4j_monitor_tx_peakTx",
                    "lastTxId", "neo4j_monitor_tx_lastTxId",
                    "currentOpenedTx", "neo4j_monitor_tx_currentOpenedTx",
                    "totalOpenedTx", "neo4j_monitor_tx_totalOpenedTx",
                    "totalTx", "neo4j_monitor_tx_totalTx"
            )
    );
}