# Query Definitions [](id=query-definitions)

Data is accessed through Repository objects. These components allow the selected persistence type to be abstracted and interact properly with the database.
Persistence or query logic (such as SQL queries) should be avoided as much as possible outside these objects precisely so as not to create relationships with the type of technology used.
In future versions of the framework, in fact, it is not ruled out that the possibility of specifying queries except through HyperIoTQuery objects will be precisely forbidden. 
To date, methods for queries such as queryForSingleResult or queryForResultList accept a query as a parameter. This approach appears to be not very robust and will need to be revised.
Currently, such methods or finder methods can be leveraged to query the database. 
We recommend always using the HyperIoTQueryBuilder and the HyperIoTQuery class that abstracts the query technology and in fact will be compatible with future versions.
In case you want to define a particularly complex query, it can be defined in the concrete Repository class of the entity related to it and expose that method externally.

Below is an example for the RoleRepositoryImpl class:

```
package it.acsoftware.hyperiot.role.repository;
...

@Component(service = RoleRepository.class, immediate = true)
public class RoleRepositoryImpl extends HyperIoTBaseRepositoryImpl<Role> implements RoleRepository {
    ...    
    @Override
    public Role findByName(String name) {
        getLog().debug( "Invoking findByName: " + name);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            getLog().debug( "Transaction found, invoke persist");

            Role entity = null;
            try {
                entity = entityManager.createQuery("from Role r where r.name = :name", Role.class).setParameter("name", name)
                        .getSingleResult();
                getLog().debug( "Entity found: " + entity);
            } catch (NoResultException e) {
                getLog().debug( "Entity Not Found ");
            }
            return entity;
        });
    }
    ...
    /**
     * Collection of user roles obtained via query
     */
    public Collection<Role> getUserRoles(long userId) {
        getLog().debug( "invoking getUserRoles, by: " + userId);
        HashMap<String, Object> params = new HashMap<>();
        params.put("userId", userId);
        return this.queryForResultList(
            "select u.roles from HUser u inner join u.roles where u.id=:userId", params, Set.class);
    }

}

```