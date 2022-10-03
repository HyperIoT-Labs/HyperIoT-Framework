# Basic Concepts [](id=basic-concepts)

The layers defined within each project (considering also al part of persistence that we said is optional) are:

* Persistence
* Service Layer
  * System Api
  * Service Api 
* Rest
* Websocket

The <b>persistence</b> layer consists of a class that generalizes basic save and search operations. Thus by extending the framework class there is no need to write any persistence logic.

The <b>Service layer</b> consists of 2 components that, implementing two interfaces <b>SystemApi</b> and <b>Api</b>.

* <b>SystemApi</b>: classes that perform validation logic on beans (leveraging javax.validation annotations and automating error handling) before they are sent to the persistence layer

* <b>Api</b>: classes that invoke *SystemApi methods but before doing so verify that the user has permissions to perform certain operations

In essence, then, the main difference between the two types of <b>SystemApi</b> and <b>Api</b> services relates to the fact that the former does not perform any verification of permissions. This is useful because when you need to do a system operation you can directly inject the system instance thus bypassing the System permission. The second one, on the other hand, before any operation checks the user's permissions.

The <b>Rest</b> Layer is based on Apache CXF and provides basic tools to define services that require authentication or not, swagger documentation generation.

The <b>Websocket</b> Layer is optional but allows a websocket to be defined easily without too much effort within Karaf.