# Ecosystem Overview [](id=ecosystem-overview)

![EcoSystem Overview](../images/hyt-ecosystem.png)

HyperIoT Framework is focused on increasing developer productivity. 
Basically it defines a structure model for projects. Developers who follows this model will inherit a lot of out of the box feature and they’ll be ready to run their projects filled with functionalities in few time.

The basic difference with framework like spring is that when you scaffold a spring project you will inherit the project structure and integrations. You then have to write down configuration code for persistence, entities, rest apis.

With HyperIoT framework everything will be generated and can be easily customized by yourself.
HyperIoT Supports these out of the box feature :

* A standard configuration for running java microservices in OSGi environment
* Generated CRUD Logic and swagger documentation for you entities and rest services
* Permission system integrated and customizable with external systems
* Integration with Hadoop, Kafka, HBase, Storm, Spark already exposed as rest services so you can just invoke these service in order to implement your integration logic.
* Permissioned API for registering data to ethereum blockchain
* Pre/Post actions for every system (or user) entity in order to implement advanced logic
* Possibility to implement pattern like “OwnedResource”. It’s a pattern where you want to manage a resource or entity which belongs only to user who has created it. For example : you account profile, or personal data. If an entity is marked as OwnedResource it is accessible only by the user who owns it.
* Possibility to implement pattern like “SharedResource”. It’s a pattern where you have an owned resource which can be shared with other users. So every owner can share entity he owns with other users.
* Every module is modular and can be extended or overidden

The platform consist in three pillars:

* A Code generator which helps you to scaffold , build, deploy ,test and check your source code
* A framework with some useful classes which helps you to avoid boilerplate code for specific topics
* A karaf custom runtime which can be generated and customized for your specific modules