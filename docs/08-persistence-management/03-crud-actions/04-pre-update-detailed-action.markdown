# PreUpdate Detailed Action(id=pre-update-detailed-action)

In HyperIoTPreUpdateDetailedAction the execute method is executed just before the entity is saved within the database.

![Pre Save Action Class Hierarchy](../../images/crud-pre-post-actions/pre-update-detailed-action.png)

To define a custom behavior hooked to a pre-update detailed action, simply register a component as follows:

```
@Component(service = HyperIoTPreUpdateDetailedAction.class, property = {"type=<entity_package_with_className>"})
public class MyPreAction <T extends HyperIoTBaseEntity> implements HyperIoTPreUpdateDetailedAction<T> {

    @Override
    public void execute(T beforeCrudActionEntity, T afterCrudActionEntity) {
      ....
    }


}
```

The "magic" happens all in the OSGi annotation :

```
@Component(service = HyperIoTPreUpdateDetailedAction.class, property = {"type=mypackage.MyEntity"})
```

By implementing the interface and specifying via property OSGi type the entity to which you want to "capture" you can easily perform custom logic in the life cycle of any entity within the HyperIoT Framework.

N.B. "With great power comes great responsibility" cit.

## Difference With PreUpdate Action

The main difference between the "detailed" and the "normal" update action lies in the method to be implemented.
In the classic action the execute method receives the updated entity, i.e. the one that already contains the effect of the updates of the update query.
In the HyperIoTPreUpdateDetailedAction the execute method receives as many as two entities, the first is "the old" i.e., the entity before performing the update, the second is the updated entity.
This type of action can be useful when you want to identify information that has changed within the entity itself. 


