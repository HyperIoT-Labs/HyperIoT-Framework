# Authorization [](id=hyperiot-authorization)

HyperIoT Framework also provides APIs that allow, by checking that users have the necessary permissions, to ensure that actions on resources and entities are performed safely.

The concept of a resource and specifically what a secure resource is and how to define actions on resources was explained earlier.
Once an action or series of actions has been defined, it is possible to associate it with the resource and a user role in order to define a permission.

The verification on permissions is done by means of the so-called permission manager. In the HyperIoTBase-api module there is the HyperIoTPermissionManager interface. The concrete implementation of this interface is used by the HyperIoTSecurityUtil utility class located within the HyperIoTBase-security module.
Since HyperIoT Framework is based on OSGi, it is possible to define custom implementations of the permission manager by implementing the HyperIoTPermissionManager interface and substituting it for the default implementation.
In the HyperIoTPermissionManager interface we find the default isProtectedEntity() methods that allow checking whether a resource is protected.

The HyperIoTSecurityUtil class, for checking permissions, offers two utility methods:

```
public static boolean checkPermission(HyperIoTContext ctx, String resourceName, HyperIoTAction action)
```

and

```
public static boolean checkPermissionAndOwnership(HyperIoTContext context, String resourceName,HyperIoTAction action, HyperIoTResource... entities)
```

There are also overloaded versions that allow the resource to be passed directly instead of the resourceName parameter.

The checkPermission method verifies that the authenticated user (retrieved from the HyperIoT context) has a role for which permission has been defined to perform the action for the resource identified by the resourceName parameter, in which case it returns true. Obviously the resource must be protected, i.e., it must implement the HyperIoTProtectedResource or HyperIoTProtectedEntity interface; in case the resource is not protected, checkPermission returns true bypassing the checks by granting permission to the user.

The checkPermissionAndOwnership method is similar to the checkPermission method but with the additional constraint that the entities belong to the user.

HyperIoT Framework provides for custom entities a default implementation of CRUD methods by also embedding the permission check for CRUD actions in the HyperIoTBaseEntityServiceImpl class.

The pattern suggested by the framework for developing new functionality is shown in the following diagram:

![Framework invocation pipe](../images/framework-invocation-pipe.png)

Control over permissions occurs within the methods defined in the *ServiceImpl classes.

Let us try to show, through an example, what has been exposed so far.
Suppose we are within a system that deals with the management of a library and we have therefore defined an entity, through the Book class, making it protected by having the class implement the HyperIoTProtectedEntity interface.
In addition to the CRUD actions that, as mentioned, the framework implements automatically, suppose we add a new action representing the lending of the book to a user.
In this scenario, the problem then arises of ensuring that only the users in charge of managing book lending in the library can perform this specific action.

We must, therefore, first define the new action within the enum that implements the HyperIoTActionName interface within the -actions module, suppose the name of the enum is BookActionName:

```
public enum LibroAction implements HyperIoTActionName {
   
   //definisce l'azione per il prestito di un libro
   LEND("lend");

   private String name;

   private LibroAction(String name) {
      this.name = name;
   }

   public String getName() {
      return name;
   }
}
```

The action was defined by creating the new LEND instance of the enum.

Still within the -actions module we need to register the new action within the getActions() method of the class that extends the HyperIoTPermissionActivator interface (suppose it is called BookActionsBundleActivator) by adding the statement:

```
actionList.addAction(HyperIoTActionFactory
    .createAction(Libro.class.getName(), Libro.class.getName(), AutoreAction.LEND));
```
Assuming we have already implemented the new lendBookToUser functionality following the above pattern, in the BookServiceImpl class we will have the method:

```
public Libro lendToUser(long bookId, long userId, HyperIoTContext context) {
  return systemService.lendBookToUser(bookId, userId);
}
```

It is within this method that the permission check must take place. Specifically, to check that the user who is lending the book has permission to lend the book, simply modify the body of the lendToUser method like this:

```
public Libro lendToUser(long bookId, long userId, HyperIoTContext context) {
  if (!HyperIoTSecurityUtil.checkPermission(context, Libro.class.getName(),
        HyperIoTActionsUtil.getHyperIoTAction(Libro.class.getName(), Libro.LEND))) {
    throw new HyperIoTUnauthorizedException();
  }

  return systemService.lendBookToUser(bookId, userId);
}
```

the following block of code is the one that takes care of checking whether the user has permission to perform the action; if not, the operation ends with the exception that HyperIoTUnauthorizedException indicating that the user is not authorized to perform the action.

```
if (!HyperIoTSecurityUtil.checkPermission(context, Libro.class.getName(),
        HyperIoTActionsUtil.getHyperIoTAction(Libro.class.getName(), Libro.LEND))) {
  throw new HyperIoTUnauthorizedException();
}
```