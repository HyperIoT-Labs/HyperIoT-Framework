# HyperIoT Framework [](id=intro)

HyperIoT OSGi Framework was born as a solution for implementing µSerivces in OSGi.

The goal behind the creation of this framework is very simple: to enable µServices-based applications to be written in a short time by trying to provide a ready-to-use framework that tries to simplify development with a number of out-of-the-box features, offering integrations and patterns for microservices-oriented development.

The framework is based on a paradigm we have coined: "Convention over coding". It means that the structure of applications follows certain mechanisms and dynamics that allows them to have truly simple code management (from development to build and deployment) and interface remoting. 
"Convention over coding" is not just a "development to a standard" but a set of techniques, tools and code conventions that ,if followed, drastically cut down development time and especially code maintenance.

This framework has solid basis and used in a lot application. Our roadmap provides to insert are µServices related patterns in the future such as: Api Gateway, Service Mesh, Support for SAGA pattern, Certificate Management and rotation, and µServices configuration management. Everything available in one place, written in a modular way so that the final user can customize it for his purpose.
Another interesting field of research would be to implement a generic cluster deployer module for Kubernetes in order to ship and balance microservices automatically based on the current resource consumption updating the service registry automatically. 

Imagine having to write a micro-service that performs the CRUD of an entity: the developer will have at his disposal a generator that will create at zero time the infrastructure underlying the micro-service in terms of: persistence layer with its transactions, entity validation layer, class layer for business logic and finally the rest services. 
The developer's task will only be to define the entity in terms of fields and related annotations for validation and finally to define the access permissions. Once this is done, the work will already be finished and the deployment of the micro-service can be done directly in the container.

The Container used for development is Apache Karaf precisely because of the possibility of creating features that directly install all the necessary dependencies, it is possible, in any case, to use any OSGi container.

Since HyperIoT Framework is based on a model rather than a tecnology we are planning to evolve this model for other frameworks like Spring or Quarkus.