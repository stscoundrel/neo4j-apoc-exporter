# Neo4j/Apoc Prometheus Exporter

Prometheus exporter for Neo4j community edition, using Apoc plugin.

## Motivation.

There is no simple way of exposing Prometheus metrics in community edition. If you are using enterprise edition, you can just enable Prometheus metrics. Same does not apply to community edition.

To expose at least some of the relevant metrics, this exporter uses Apocs monitor commands. Therefore your Neo4j installation needs Apoc Extended plugin installed.


