# Convention Over Coding [](id=convention-over-coding)

The way it is described, our solution sounds very similar to the classic "Convention over configuration." In reality, it is not.

The so-called "Convention over configuration" reduces the configurations to be specified by the developer who can rely on some "default" conventions of the technology used. 

One example out of all: @Entity on a Java class will associate a table with the class name if not specified. 
This type of approach is certainly useful and is currently used within our framework, but we have decided to go even further.

Below is a list of out of the box features available by adopting the framework and following the convention:

* Automatic user profiling
* Automatic management of permissions and authorizations
* User impersonation
* Formal validation of entities
* Error handling
* Automatic swagger documentation generation
* Automatic generation of rest services
* Simplified websocket management with automatic authentication support
* Native integration with Kafka as a service bus for managing shared messaging between various application layers or clusters
* Modules exportable and usable in any OSGi context and integrable with other technologies
* Ability to create clusters without additional configuration thanks to Zookeeper
