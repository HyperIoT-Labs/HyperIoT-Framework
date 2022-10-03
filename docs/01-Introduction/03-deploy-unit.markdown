# Deploy Unit is Micro! [](id=deploy-unit)

The framework encourages the development of individual self-consistent modules that constitute a deployment unit in themselves. 
With this approach, deployments are also "micro" by always involving only individual "building blocks" of the application.

From this point of view it will then be up to the developer to decide how to deploy the microservice with 2 possibilities:

* A Karaf instance (or osgi container) with n modules + HyperIoTCore inside.
* A Karaf instance (or osgi container) with 1 module + HyperIoTCore inside.

In the second case we "fall back" to the common approach to micro-services development where there is a linear growth of containers as services increase. 
In the first case, on the other hand, a single container can contain more than one container.

In addition, OSGi allows each individual "micro-service" to be hot updated on time without the need for a restart. 
This becomes an additional element in favor of this technology that allows through a macro-container to possibly host multiple micro-services. 

Wait! we are not saying that it has to be done, but the possibility of being able to cluster simple micro-services at low load could become a great way to reduce the number of circulating containers.