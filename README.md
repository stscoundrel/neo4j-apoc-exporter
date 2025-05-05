# Neo4j/Apoc Prometheus Exporter

[Prometheus](https://prometheus.io/) exporter for [Neo4j](https://neo4j.com/) community edition, using [Apoc plugin](https://neo4j.com/labs/apoc/5/).

## Requirements
- Neo4j installation (community and enterprise should both work)
- Apoc Extended plugin installed.

## Installation

The exporter is provided as Docker image (to be published in hub, currently available in this repo). You can also build the Jar yourself from sources and run that, should you have a special use case.

The image accepts the following environment variables:

- `NEO4J_URI`
- `NEO4J_USER`
- `NEO4J_PASSWORD`
- `EXPORTER_PORT`

For example on usage with Docker compose, see `example` folder.

## Metrics

The exporter exposes (at least) the following metrics:

```
neo4j_monitor_ids_relIds
neo4j_monitor_ids_nodeIds
neo4j_monitor_ids_propIds
neo4j_monitor_ids_relTypeIds
neo4j_monitor_store_propStoreSize
neo4j_monitor_store_nodeStoreSize
neo4j_monitor_store_logSize
neo4j_monitor_store_stringStoreSize
neo4j_monitor_store_arrayStoreSize
neo4j_monitor_store_totalStoreSize
neo4j_monitor_store_relStoreSize
neo4j_monitor_tx_peakTx
neo4j_monitor_tx_rolledBackTx
neo4j_monitor_tx_currentOpenedTx
neo4j_monitor_tx_totalOpenedTx
neo4j_monitor_tx_totalTx
neo4j_monitor_tx_lastTxId
```

Is a relevant metric missing that is available in community edition? Open an issue or PR.

## Motivation.

There is no simple way of exposing Prometheus metrics in community edition. If you are using enterprise edition, you can just enable Prometheus metrics. Same does not apply to community edition.

To expose at least some of the relevant metrics, this exporter uses Apocs monitor commands.


