# Security Annotations [](id=hyperiot-security-annotations)

Permission verification is an operation that requires entering the same code over and over again. For this reason, annotations have been created through HyperIoT interceptors to automatically handle permission verification:

* @AllowGenericPermission 
* @AllowPermission 
* @AllowPermissionOnReturn 
* @AllowRoles

Each of these three annotations has a different purpose, let's see which one.

The annotations can only be used on methods of classes that extend HyperIoTService.

Automatically every *Api and every *SystemApi fall under this.

In addition, the annotated method must receive exact parameters based on the annotation used. One of the parameters that must always be present is the context i.e. HyperIoTContext . This turns out to be essential in order to get the info about the current logged in user. To obtain such an object in the *ServiceImpl just use the getHyperIoTContext() method from where it is automatically obtained from the container security context.

## @AllowGenericPermission

This annotation can be used on a method of an *Api or *SystemApi allows to automatically verify a given permission on a generic HyperIoTResource.

The usage is as follows:

```
/**
* Updates user account only if the user is changing his personal info
*/
@Override
@AllowPermissions(actions = HyperIoTCrudAction.Names.UPDATE)
  public HUser adminUpdateAccountInfo(HyperIoTContext context, HUser user) {
      HUser loggedUser = this.systemService.findUserByUsername(context.getLoggedUsername());
      if (loggedUser == null) {
          throw new HyperIoTUnauthorizedException();
      }
      return doUpdateAccountInfo(user, context);
  }
```

The annotation requires the inclusion of the list of Actions, that is, the list of names of actions that are to be checked.

In this case, the user logged in to execute the method will need to have update permission on the resource <b>it.acsofware.hyperiot.huser.model.HUser</b>.

The resource is automatically inferred if you are within a *BaseEntityServiceImpl or *BaseEntitySystemService.

If, on the other hand, you are using a service not associated with an entity (so *BaseServiceImpl or *SystemServiceImpl) there will be another mandatory parameter: resourceName.

Below is an example of code taken from the HBaseConnector:

```
@Override
@AllowGenericPermissions(actions = HBaseConnectorAction.Names.CHECK_CONNECTION, resourceName = HBASE_CONNECTOR_RESOURCE_NAME) 
  public void checkConnection(HyperIoTContext context) throws IOException, HyperIoTUnauthorizedException, ServiceException {
      systemService.checkConnection();
}
```

In this case, in addition to specifying the action "CHECK_CONNECTION" to be checked, the resourceName is entered.

## @AllowPermission

This annotation is more specific than the previous one: Whereas with @AllowGenericPermission one goes to check the existence of a permission on a resource-name (thus not considering the specific resource), with @AllowPermission one can check the permission on a specific entity. The annotation allows the system to self-infer both the resource-name and the entity (which must be an argument of the method) to be checked, let's see an example:

```
/**
  * Update an existing entity in database
  *
  * @param entity parameter that indicates a generic entity
  * @param ctx    user context of HyperIoT platform
  */

@AllowPermissions(actions = {HyperIoTCrudAction.Names.UPDATE})
public T update(T entity, HyperIoTContext ctx) {
    this.log.debug("Service Updating entity entity {}: {} with context: {}", new Object[]{this.type.getSimpleName(), entity, ctx});
    if (entity.getId() > 0) {
        return this.getSystemService().update(entity, ctx);
    }
    throw new HyperIoTEntityNotFound();
}
```

Although this method is generic, the type T extends HyperIoTBaseEntity, this is sufficient for the system to recognize the "entity" parameter as the entity object on which to verify the permission.

As an alternative to verification by passing an entity object, it is also possible to verify a permission by passing the id of the entity in question as an argument to the method. 

```
/**
     * Remove an entity in database
     *
     * @param id  parameter that indicates a entity id
     * @param ctx user context of HyperIoT platform
     */
    @AllowPermissions(actions = HyperIoTCrudAction.Names.REMOVE, checkById = true)
    public void remove(long id, HyperIoTContext ctx) {
        this.log.debug("Service Removing entity {} with id {} with context: {}", new Object[]{this.type.getSimpleName(), id, ctx});
        HyperIoTBaseEntity entity = this.getSystemService().find(id, ctx);
        this.getSystemService().remove(entity.getId(), ctx);
    }
```

By passing the "checkById" attribute to true, the interceptor will check that the method parameters include a long. By default the id must be the first parameter otherwise the index corresponding to the "id" argument can also be specified.

```
/**
 * Save a user role
 *
 * @param userId parameter required to find an existing user
 * @param roleId parameter required to save a user role
 * @param ctx    user context of HyperIoT platform
 * @return the user's role saved
 */
@AllowPermissions(actions = HyperIoTRoleAction.Names.ASSIGN_MEMBERS,checkById = true,idParamIndex = 1)
public Role saveUserRole(long userId, long roleId, HyperIoTContext ctx) {
    getLog().debug("invoking saveUserRole, save role: {}  from user: {}", new Object[]{roleId, userId});
    Role r = null;
    try {
        HUser u = this.userSystemService.find(userId, ctx);
        r = this.systemService.find(roleId, ctx);
        u.addRole(r);
        userSystemService.update(u, ctx);
    } catch (NoResultException e) {
        getLog().debug("invoking saveUserRole, HUser not found! ");
        throw new HyperIoTEntityNotFound();
    }
    return r;
}
```

In the case shown above passing "idParamIndex=1" (meaning the second argument because the first has index 0) the id used for permission verification will be associated with the variable "roleId".

## @AllowPermissionOnReturn

This annotation performs the same check as @AllowPermission but on the type returned by a method.

## @AllowRoles

With this annotation, it can be verified that only users with a certain role can access a certain method.



