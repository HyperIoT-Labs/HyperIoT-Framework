# Persitence Class Hierarchy [](id=persistence-class-hierarchy)

The hierarchy of persistence classes within the HyperIoT Framework is as follows:

![Persistence Inheritance](../images/persistence-inheritance.png)

We start, clearly from the concept of a resource: HyperIoTBaseEntity is nothing but a HyperIoT resource with an associated persistence layer. 
For that reason, such an interface, defines all typical entity methods. 
Clearly many of the persistence logic of an entity can already be implemented, for this reason, an initial abstract declination of the interface was created called, precisely, HyperIoTAbstractEntity.

Each entity then has a Repository that encapsulates all the persistence and query logic.

N.B. in the future it will be necessary to introduce private constructors and remove some setters that, although they represent standard java beans, go against some principles of domain driven design. For example, the createDate of an entity should not be exposed by setter method but defined at the time of its construction.