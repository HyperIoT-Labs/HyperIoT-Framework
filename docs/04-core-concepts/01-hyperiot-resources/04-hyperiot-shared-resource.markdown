# Shared Entities: HyperIoTSharedEntity[](id=hyperiot-shared-resource)

The concept of an owned entity can be easily extended so that the entity can also be shared with other users.

For this purpose HyperIoT framework provides the <i>HyperIoTSharedEntity</i> interface.
The HyperIoTSharedEntity interface does not declare any methods (it is a marker interface) and extends the <i>HyperIoTOwnedResource</i> interface, making shared entities owned entities as well.

Making a shared entity requires the same effort as is expended to make an owned entity. The only difference, in fact, lies in the fact that the entity implements HyperIoTSharedEntity instead of HyperIoTOwnedResource.

Consider the same example seen in the section on owned resources. You can make the entity Book simply by replacing the HyperIoTOwnedResource interface with the HyperIoTSharedEntity interface:

```
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "isbn") })
public class Libro extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTSharedEntity {
  ...
}
```

Similarly, the LibroServiceImpl class needs to implement the HyperIoTSharingEntityService interface. This interface extends the HyperIoTOwnershipResourceService interface:

```
public final class LibroServiceImpl extends HyperIoTBaseEntityServiceImpl<Libro> 
    implements LibroApi, HyperIoTSharingEntityService {
  ...
}
```

The HyperIoT framework provides the <b>HyperIoTSharedEntity</b> project that allows shared resources to be operated on through services that allow entities to be shared with other users.

To share an entity with other users requires that the entity belongs to the owner user and that the owner user has permission to perform the share action on the entity.
The share action is defined within the HyperIoTShareAction enum within the HyperIoTBase-api module.