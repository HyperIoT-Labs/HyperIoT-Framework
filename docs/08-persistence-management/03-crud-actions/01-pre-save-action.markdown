# PreSave Action [](id=pre-save-action)

In HyperIoTPreSaveAction, the execute method is executed just before the entity is saved within the database. 

![Pre Save Action Class Hierarchy](../../images/pre-save-action.png)

To define a custom behavior hooked to a pre-save action, simply register a component as follows:

```
@Component(service = HyperIoTPreSaveAction.class, property = {"type=<entity_package_with_className>"})
public class MyPreAction <T extends HyperIoTBaseEntity> implements HyperIoTPreSaveAction<T> {

    @Override
    public void execute(T entity) {
      ....
    }

}
```

The "magic" happens all in the OSGi annotation :
```
@Component(service = HyperIoTPreSaveAction.class, property = {"type=mypackage.MyEntity"})
```

By implementing the interface and specifying via property OSGi type the entity to which you want to "capture" you can easily perform custom logic in the life cycle of any entity within the HyperIoT Framework.

N.B. "With great power comes great responsibility" cit.