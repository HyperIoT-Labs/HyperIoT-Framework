# Apache Storm [](id=apache-storm)

Apache Storm is a computational framework for distributed stream processing written primarily in the Clojure programming language. 
Originally created by Nathan Marz and the BackType team, the project was open source after being acquired by Twitter.

The use of Storm within the HyperIoT Framework makes it possible in zero time to be able to develop integrations that allow the deployment and control of processing topology directly from a microservices layer.
Therefore, the integration mechanism provided by the HYT Framework involves the installation of all the bundles necessary to properly interface with a Storm cluster.

## Storm Bundle Management

The HyperIoTStorm Bundle provides no source code, but it does provide for the proper installation in the OSGi container of all the dependencies needed to interface with storm. Below is an excerpt of all the bundles that are included:

```
dependencies {
    compile group: "org.apache.storm", name:"storm-core", version: project.stormCoreVersion
    compile group: "org.apache.storm", name:"storm-kafka-client", version: project.stormCoreVersion
    compile group: "org.apache.storm", name:"storm-hdfs",version: project.stormCoreVersion
    compile group: 'org.apache.storm', name:'storm-hbase', version: project.stormCoreVersion
    compile group: 'org.apache.storm', name:'flux-core', version: project.stormCoreVersion
    compile group: 'org.apache.storm', name:'flux-wrappers', version: project.stormCoreVersion
    compile group: 'commons-lang', name:'commons-lang', version: '2.6'
}
```

By installing such a bundle, the modules will be available and you will be able to develop your own integrations.

It is planned to include a Client in the framework that makes a whole range of operations available. 