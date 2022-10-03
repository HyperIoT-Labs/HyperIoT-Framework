# Application Basic Structure [](id=application-basic-structure)

The first concept to be introduced is the difference between project and module. 
A project is a self-consistent entity that has a specific purpose, for example: CRUD of the User entity. 
In order to accomplish its purpose, the project is composed of different modules that further "break up" what needs to be done in a way that is always self-consistent and modular, always promoting cohesion and decoupling. 

For this reason any project will always have this basic division:

User

* User-api
* User-model *
* User-repository * 
* User-actions *
* User-service
* User-service-rest * 

Each individual module is an OSGi bundle, all modules are contained in the "parent" project that manages the lifecycle. 
Versioning is controlled by the parent via gradle. It will be possible to build and deploy projects or individual modules.

The "*" indicates that some modules are not required, this is because when developing a µSerivce this could be of two different types:

1. <strong>Entity service</strong> : requires persistence
2. <strong>Service</strong>: does not require persistence but it is an integration service.

In case 2, it will be possible not to generate the "model" and "repository" modules. 
At any time it will be possible to add a module, even if not previously generated. 

Moreover, it is possible to add custom modules in addition to the basic ones in a simple and intuitive way by taking advantage of the generator.