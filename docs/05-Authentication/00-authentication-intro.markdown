# Authentication [](id=hyperiot-authentication)

Currently within HyperIoT there are two modes of authentication provided:

* Basic
* JWT

The framework provides the possibility of defining the concept of "Authenticable" within the services. In fact nowadays authentication is not necessarily always done by a user (human), it could also be done by a device or an application.

To maintain its generality, the framework also allows the definition of custom authentication modes with associated authenticable entities.

As a default mechanism, of course, HyperIoT allows a user provided with a user and password to be able to authenticate and possibly receive a JWT token.

The concepts made available then are two:

1. <b>HyperIoTAuthenticable</b>: Interface indicating an entity (because surely it will have to be made persistent somewhere) that has certain properties typical of those entities that require login:

    * ScreenName: username in the case of a human user, deviceName in the case of a device for example 
    * isAdmin: boolean indicating whether the user is associated with an administrator profile 
    * Roles: the roles associated with the user 
    * password and password confirm for registration 
    * Active: whether the user is active or not

2. <b>HyperIoTAuthenticationProvider</b>: Interface that defines the contract to be fulfilled in case a class wants to register as an Authentication Provider of some Authenticable defined

Some explanatory examples will be given throughout the paragraphs.