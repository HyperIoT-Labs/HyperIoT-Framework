# Owned Resources: HyperIoTOwnedResource[](id=hyperiot-owned-resource)

HyperIoT framework allows a resource or entity to be enriched by providing the concept of an <b>owned resource</b>. Making a resource or entity owned means associating a user owner with the entity, that is, a user who is considered the owner of the resource or entity.

When a resource is defined as owned, the permission manager will only allow actions to be performed on the resource if the user who is performing the action is also the owner of the resource.
In addition, the 'find' and 'find all' services on owned entities will only return entities that belong to the user who is performing the action.

In order to render resources or entities of type owned, they must implement the HyperIoTOwnedResource interface defined within the HyperIoTBase-api module.
The HyperIoTOwnedResource interface defines the ```HyperIoTUser getUserOwner()``` method that allows resources implementing the interface to define their own user owner.

Let us see through an example how to render an entity of type owned.
Consider the entity Book defined as follows:

```
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "isbn") })
public class Libro extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {
    private String title;
    private String isbn;
    private Autore autore;
    
    ...
}
```
We want to extend the Book entity by defining the concept that books belong to users. To do this we need Book to implement the HyperIoTOwnedResource interface:

```
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "isbn") })
public class Libro extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTOwnedResource {
    private String title;
    private String isbn;
    private Autore autore;
    
    ...
}
```
Once the interface is implemented, we need to implement the <i>getUserOwner()</i> method.
At this point we can introduce a many-to-one relationship between Book and HUser (the entity that represents users in the framework):

```
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "isbn") })
public class Libro extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTOwnedResource {
    private String title;
    private String isbn;
    private Autore autore;
    
    private HUser user;
    @ManyToOne(targetEntity = HUser.class)
    public HUser getUser() {
        return user;
    }
    
    @Override
    @Transient
    @JsonIgnore
    public HyperIoTUser getUserOwner() {
        return user;
    }
    ...
}
```
In the code snippet just shown, it is important to note that the <i>getUserOwner()</i> method returns user directly since HUser extends HyperIoTUser.

At this point, to ensure that when the find service is invoked, but more importantly find all, entities of type Book belonging to the user invoking the service are retrieved, the LibroServiceImpl class must also be modified by having it implement the <i>HyperIoTOwnershipService</i> interface provided by the HyperIoT framework.
The HyperIoTOwnershipService interface allows you to define, by implementing its abstract method getOwnerFieldPath(), the id attribute of the user owner defined in the entity:

```
@Component(service = LibroApi.class, immediate = true)
public final class LibroServiceImpl extends HyperIoTBaseEntityServiceImpl<Libro> 
    implements LibroApi, HyperIoTOwnershipResourceService {
    
    @Override
    public String getOwnerFieldPath() {
        return "user.id";
    }
    ...
}
```
In the example, the <i>getOwnerFieldPath()</i> method returns the string "user.id" because in the Book entity, the attribute of type HUser has name "user"; in general, the method should return a string of type <attributeNameHUser>.id.
The string returned by getOwnerFieldPath() will be used to filter entities by user owner during the query.