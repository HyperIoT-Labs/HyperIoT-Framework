# HyperIoTQuery (id=hyperiot-query)

HyperIoT Framework allows some repository methods to be invoked by passing HyperIoTQuery as an argument. This class represents the abstraction (which will be exploited more and more within the framework) to implement custom query logic that is, however, agnostic to the currently used persistence layer.

To create custom queries, one can use the dedicated HyperIoTQueryBuilder :

```
HyperIoTQuery idFilter = HyperIoTQueryBuilder.newQuery().equals("id", id);
```

Currently there is no particularly complex logic in fact it is also possible to perform articulated queries on entities:

```
 HyperIoTQueryBuilder.newQuery().equals(ownedRes.getOwnerFieldPath(), ctx.getLoggedEntityId())
                        .or(HyperIoTQueryBuilder.newQuery().in("id", entityIds));
```

The use of such a component is strongly recommended because in future versions it will increasingly allow generic queries to be written, being able to then convert them to different repository technologies such as traditional relational databases or noSQL.