# Apache Hadoop [](id=apache-hadoop)

Hadoop HDFS is integrated into HyperIoT to support the so-called batch layer of the Lambda Architecure. 
It is a distributed, scalable and fault-tolerant file system. 
It relies on HBase, a NoSQL database also integrated into HyperIoT, through which to implement, in contrast, the speed layer and presentation layer of the same architecture.

Essentially, HyperIoT on it stores packet data from IoT networks, thereby providing support for jobs to acquire data on which to perform processing. Jobs, simultaneously, can leverage it to share information (example: worker nodes in a Spark cluster access external jars as well as the job jar via HDFS).
Integration is achieved through a classic HyperIoT project, which carries, as an inherent dependency, the HDFS client (currently version 2.7.4).

Any interaction with HDFS is exposed through the api module, which is why the next chapter will detail the operations currently supported.

## Hadoop Manager System Api Interface

![Hadoop System Api Interface](../../images/hadoop-manager-system-api.png)

Details for each of the exposed methods follow:

* ```void copyFile(File file, String path, boolean deleteSource)```: copies the file to the HDFS path. If deleteSource is true and a file already exists at the specified path, then the latter is overwritten with the new one.
* ```void deleteFile(String path)```: deletes the file at the specified path.
* ```void deleteFolder(String path)```: deletes the directory at the specified path.

## Configurations

In the Karaf distribution there is the file en.acsoftware.hyperiot.hadoopmanager.cfg for module configuration.

The properties in the file follow:

| Property                                                 | Default | Description |
|----------------------------------------------------------|---------|-------------|
| it.acsoftware.hyperiot.hadoopmanager.defaultFS  | hdfs://namenode:8020      | HDFS URI