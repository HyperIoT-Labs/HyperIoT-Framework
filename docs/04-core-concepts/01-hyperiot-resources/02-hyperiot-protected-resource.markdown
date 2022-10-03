# Protected Resources: HyperIoTProtectedResource[](id=hyperiot-protected-resource)

One of the recurring problems in a Web portal or software system is that not all users have the same privileges on resources. Depending on the type of user depends on the actions that can be performed on the resources.

One can think, for example, of a portal present within a library. Certainly, it is possible to distinguish two types of users: users whose task is to census and update the catalog of books, and ordinary users who can only consult the catalog.
Therefore, the problem arises of making the page containing the form for entering books into the catalog inaccessible to ordinary users.
This problem is solved by making the resources subject to permission control.

HyperIoT Framework allows, through the <i>HyperIoTProtectedResource</i> interface, to extend the concept of a resource seen in the previous paragraph by making it <b>"protected"</b>, that is, subject to the so-called "check" on permissions.
The <i>HyperIoTProtectedResource</i> interface defines the <i>getResourceId()</i> method which, together with the <i>getResourceName()</i> method inherited from the <i>HyperIoTResource</i> interface, concurs to define the resource.
The value returned by <i>getResourceId()</i> will be used by the permission manager to control permissions on the resource itself.

It was mentioned in the previous section that entities are special types of resources and, as such, they too may be subject to permission control.

Taking the library example again, entities of type <i>Book</i>, from which the catalog can be thought to be composed, are subject to different permissions depending on the type of user; that is, ordinary users will only have permission to perform the find and find all actions on the entity but not the save, update and remove actions.

For this purpose, in HyperIoT Framework, there is the <i>HyperIoTProtectedEntity</i> interface.
HyperIoTProtectedEntity is what is referred to in the jargon as a marker interface; in fact, it defines no methods and extends the base interface for <i>HyperIoTBaseEntity</i> entities (note that HyperIoTBaseEntity in turn extends HyperIoTResource demonstrating that entities are special types of resources).

Entities that implement HyperIoTProtectedEntity can then be considered protected resources thus making them subject to the permissions check.

The following is the hierarchy of interfaces that define resources and entities:

![Framework invocation pipe](../../images/hyt-resource-hierarchy.png)