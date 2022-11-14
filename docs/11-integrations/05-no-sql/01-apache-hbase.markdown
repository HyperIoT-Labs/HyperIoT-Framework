# Apache HBase [](id=apache-hbase)

Apache HBase is a NoSQL database supporting the lambda architecture, specifically for implementing the presentation layer.
In addition, with reference to the HyperIoT application, it provides support for extracting, in offline mode, saved data.

The currently supported version of HBase is 1.4.9.

## Available Operations

The project is nothing but a container of the HBase client, of which the basic operations are re-expressed, viz:

* connection status check;
* table creation/disabling/removal;
* adding/removing/retrieving data.

## Configurations

In the Karaf distribution there is the file en.acsoftware.hyperiot.hbase.connector.cfg for module configuration.

The properties in the file follow:

| Property                                                | Default                         | Description |
|---------------------------------------------------------|---------------------------------|-------------|
| it.acsoftware.hyperiot.hbase.connector.client.scanner.max.result.size  | 50                              | Maximum number of bytes returned upon a call of the next method on the Scanner object
| it.acsoftware.hyperiot.hbase.connector.cluster.distributed  | true                            | Cluster mode (false for standalone cluster, true for distributed mode)
| it.acsoftware.hyperiot.hbase.connector.master  | hbase-test.hyperiot.cloud:16000 | Master address
| it.acsoftware.hyperiot.hbase.connector.master.hostname  | hbase-test.hyperiot.cloud       | Hostname del Master
| it.acsoftware.hyperiot.hbase.connector.master.info.port  | 16010                           | WebUI Master Port
| it.acsoftware.hyperiot.hbase.connector.master.port  | 16000                           | Master port
| it.acsoftware.hyperiot.hbase.connector.regionserver.info.port  | 16030                           | Region Server WebUI Port
| it.acsoftware.hyperiot.hbase.connector.regionserver.port  | 16020                           | Region Server Port
| it.acsoftware.hyperiot.hbase.connector.rootdir  | hdfs://namenode:8020/hbase      | HBase base path
| it.acsoftware.hyperiot.hbase.connector.zookeeper.quorum  | zookeeper-1.hyperiot.com        | Zookeeper server list
| it.acsoftware.hyperiot.hbase.connector.await.termination  | 1000                            | Time interval for all tasks to be completed when the application is stopped
| it.acsoftware.hyperiot.hbase.connector.core.pool.size  | 10                              | Initial number of threads to support asynchronous operations
| it.acsoftware.hyperiot.hbase.connector.keep.alive.time  | 0                               | Interval of time that threads remain in the idle state
| it.acsoftware.hyperiot.hbase.connector.maximum.pool.size  | 10                              | Maximum number of threads to support asynchronous operations


