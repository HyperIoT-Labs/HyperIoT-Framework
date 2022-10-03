# Defining Entity Permissions [](id=defining-entity-permissions)

In HyperIoT Framework, a permission is defined by linking one or more actions defined on an entity to a user role.

In addition to the actions representing so-called CRUD operations, which are automatically defined by the framework, it is possible to define new actions on an entity in a very simple way. It is enough, in fact, to create instances of the *Action enum that abstractly define the action, and to register the new actions in the OSGi context within the getActions() method of the *ActionsBundleActivator class; both the *Action enum and the *ActionsBundleActivator class are located within the *-actions module.

After defining actions it is possible, through the REST GET hyperiot/permissions/actions service, to view all registered actions broken down by entity.

To define a permission you need to create an instance of the Permission entity (defined within the HyperIoTPermission project) by invoking the REST POST hyperiot/permissions service. The REST service receives as a request body parameter the JSON in the format:

```
{
    "name":string
    "role":{
        "id”:long
    },
    "actionIds":int,
    "entityResourceName":string
}
```
The name key allows you to specify the name of the permission; entityResourceName allows you to determine which entity the actions are defined for; role.id allows you to assign the permission to the user via the role id; actionIds on the other hand allows you to specify which actions are defined for the permission.
For actionIds, it is possible to assign the sum of the values returned by the actionId attributes of each action.

Let us now see, through an example, how to put the above into practice.

Suppose we have two roles, GUEST_USER and BACKOFFICE_USER, to assign to users in a library. We now want to define permissions for these two roles on the Book entity. Specifically, GUEST_USER will only have to have permission for find and find all actions, while BACKOFFICE_USER can perform all CRUD actions, so save, update, remove, find, and find all.

The REST GET hyperiot/permissions/actions service returns for the Book entity the actions:

```
"it.acsoftware.libreria.libro.model.Libro": [
    {
        "resourceName": "it.acsoftware.libreria.libro.model.Libro",
        "actionName": "save",
        "category": "it.acsoftware.libreria.libro.model.Libro",
        "actionId": 1,
        "registered": true
    },
    {
        "resourceName": "it.acsoftware.libreria.libro.model.Libro",
        "actionName": "update",
        "category": "it.acsoftware.libreria.libro.model.Libro",
        "actionId": 2,
        "registered": true
    },
    {
        "resourceName": "it.acsoftware.libreria.libro.model.Libro",
        "actionName": "remove",
        "category": "it.acsoftware.libreria.libro.model.Libro",
        "actionId": 4,
        "registered": true
    },
    {
        "resourceName": "it.acsoftware.libreria.libro.model.Libro",
        "actionName": "find",
        "category": "it.acsoftware.libreria.libro.model.Libro",
        "actionId": 8,
        "registered": true
    },
    {
        "resourceName": "it.acsoftware.libreria.libro.model.Libro",
        "actionName": "find-all",
        "category": "it.acsoftware.libreria.libro.model.Libro",
        "actionId": 16,
        "registered": true
    }
]
```

At this point we can create the permission to be associated with the GUEST_USER role by invoking the REST POST hyperiot/permissions service passing the JSON as the body of the request:

```
{
    "name":"GUEST_PERMISSION"
    "role":{
        "id":1
    },
    "actionIds":24,
    "entityResourceName":"it.acsoftware.libreria.libro.model.Libro"
}
```

Since users with the GUEST_USER role must be able to support find and find all actions, actionIds contains the sum of the actionId value of the find action (8) and the actionId value of the find all action (16). For simplicity, it was considered that the role id GUEST_USER had value 1.

Similarly, it is possible to create the permission for users who have the GUEST_BACKOFFICE role, using JSON as the request body parameter:

```
{
    "name":"BACKOFFICE_PERMISSION"
    "role":{
        "id":2
    },
    "actionIds":31,
    "entityResourceName":"it.acsoftware.libreria.libro.model.Libro"
}
```

In this case, since users with the GUEST_BACKOFFICE role must be able to perform all actions, in this case, supported, actionIds contains the sum of the actionIds of save (1), update (2), remove (4), find (8), and find all (16). For simplicity, the role id GUEST_BACKOFFICE was considered to have value 2.