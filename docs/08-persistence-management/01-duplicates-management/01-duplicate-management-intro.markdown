# Duplicate Management And Database Unique Keys [](id=duplicate-management)

We know that in hibernate the definition of a unique key is done through the @UniqueConstraint annotation.

Within this annotation, placed on the class, you can specify the fields that constitute the unique key :


```
@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "username"),
		@UniqueConstraint(columnNames = "email") })
...
public class HUser extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTUser {
	...
}
```

the Framework exploits this annotation to create at runtime checks for uniqueness (by running dynamic queries) so as to perform a check for existence of duplicates before the entity itself is saved to the database.
This allows for timely and automatic error handling that, in case of duplicates, will report the detail of the field that generated the "Duplicate Entity" error. In addition, this type of error is automatically converted into a JSON response (for any REST services) reporting the above details.
Again , taking full advantage of all the technologies involved and inserting a simple annotation the framework will handle everything independently without having to add additional code, but simply annotating the source entity.