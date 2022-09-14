package it.acsoftware.hyperiot.permission.repository;

import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import it.acsoftware.hyperiot.permission.api.PermissionRepository;
import it.acsoftware.hyperiot.permission.model.Permission;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.model.Role;
import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.*;


/**
 * @author Aristide Cittadino Implementation class of the PermissionRepository.
 * It is used to interact with the persistence layer.
 */
@Component(service = PermissionRepository.class, immediate = true)
public class PermissionRepositoryImpl extends HyperIoTBaseRepositoryImpl<Permission>
        implements PermissionRepository {
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Injecting role repository
     */
    private RoleRepository roleRepository;

    /**
     * Constructor for a PermissionRepositoryImpl
     */
    public PermissionRepositoryImpl() {
        super(Permission.class);
    }

    /**
     * @return The current jpa related to database operations
     */
    @Override
    protected JpaTemplate getJpa() {
        getLog().debug("invoking getJpa, returning: {}", jpa);
        return jpa;
    }

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Reference(target = "(osgi.unit.name=hyperiot-permission-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug("invoking setJpa, setting: {}", jpa);
        this.jpa = jpa;
    }

    /**
     * @return Current role repository
     */
    public RoleRepository getRoleRepository() {
        return roleRepository;
    }

    /**
     * Injecting role repository
     *
     * @param roleRepository
     */
    @Reference
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Find a permission by a specific role and resource
     *
     * @param role     parameter required to find role by roleId
     * @param resource parameter required to find a resource
     * @return Permission if found
     */
    @Override
    public Permission findByRoleAndResource(HyperIoTRole role, HyperIoTResource resource) {
        getLog().debug("invoking findByRoleAndResource Role: {} Resource: {}", new Object[]{role, resource.getResourceName()});
        return this.findByRoleAndResourceName(role, resource.getResourceName());
    }

    /**
     * Find a permission by a specific role and resource name via query
     *
     * @param role               parameter required to find role by roleId
     * @param entityResourceName parameter required to find a resource name
     * @return Permission if found
     */
    @Override
    public Permission findByRoleAndResourceName(HyperIoTRole role, String entityResourceName) {
        getLog().debug("invoking findByRoleAndResourceName Role: {} Resource: {}", new Object[]{role, entityResourceName});
        return jpa.txExpr(TransactionType.Required, entityManager -> {
            Permission p = null;
            try {
                p = entityManager.createQuery(
                                "from Permission p where p.role.id = :roleId and p.entityResourceName = :entityResourceName and p.resourceId = 0",
                                Permission.class).setParameter("roleId", role.getId())
                        .setParameter("entityResourceName", entityResourceName).getSingleResult();
            } catch (NoResultException e) {
                getLog().debug(e.getMessage(), e);
            }
            return p;
        });
    }

    /**
     * Find permissions by a specific role
     *
     * @param role parameter required to find role by roleId
     * @return Permissions if found
     */
    @Override
    public Collection<Permission> findByRole(HyperIoTRole role) {
        getLog().debug("invoking findByRoleAndResourceName Role: {}", role.getName());
        return jpa.txExpr(TransactionType.Required, entityManager -> {
            return entityManager
                    .createQuery("from Permission p where p.role.id = :roleId",
                            Permission.class)
                    .setParameter("roleId", role.getId()).getResultList();
        });
    }


    /**
     * Find a permission by a specific role, resource name and resource id via query
     *
     * @param role               parameter required to find role by roleId
     * @param entityResourceName parameter required to find a resource name
     * @param id                 parameter required to find a resource id
     * @return Permission if found
     */
    @Override
    public Permission findByRoleAndResourceNameAndResourceId(HyperIoTRole role,
                                                             String entityResourceName, long id) {
        getLog().debug("invoking findByRoleAndResourceNameAndResourceId Role: {}", new Object[]{role, entityResourceName, id});
        return jpa.txExpr(TransactionType.Required, entityManager -> {
            return entityManager.createQuery(
                            "from Permission p where p.role.id = :roleId and p.entityResourceName = :entityResourceName and p.resourceId = :id",
                            Permission.class).setParameter("roleId", role.getId())
                    .setParameter("entityResourceName", entityResourceName).setParameter("id", id)
                    .getSingleResult();
        });
    }

    /**
     * Checks if  default "RegisteredUser" role exists, and, if not, creates it.
     */
    public void checkOrCreateRoleWithPermissions(String roleName, List<HyperIoTAction> actions) {
        jpa.tx(TransactionType.Required, entityManager -> {

            Role r = checkOrCreateRole(roleName);
            HashMap<String, Integer> actionsIds = new HashMap<>();
            HashMap<String, Permission> existingPermissions = new HashMap<>();


            //calculating pairs resourceName - actionsIds
            for (int i = 0; i < actions.size(); i++) {
                HyperIoTAction action = actions.get(i);
                //Checks if permission already exists for that resource
                Permission p = this.findByRoleAndResourceName(r, action.getResourceName());
                if (p == null)
                    getLog().debug("No permission found for resource: {} and role {}", new Object[]{action.getResourceName(), r.getName()});
                else
                    existingPermissions.put(action.getResourceName(), p);

                // create pair <resourceName, actionId> if resourceName does not exist, sum actionId otherwise
                actionsIds.merge(action.getResourceName(), action.getActionId(), Integer::sum);
            }

            checkOrCreatePermission(actionsIds,existingPermissions, r,0L);
        });
    }

    /**
        Check if role exist :
                1)if not exist create role with permission to specific Entity
                2)if exist search if role's permission must be updated
     The permission associated is relative to a specific entity.
     */
    @Override
    public void checkOrCreateRoleWithPermissionsSpecificToEntity(String roleName, long entityId, List<HyperIoTAction> actions) {
        jpa.tx(TransactionType.Required, entityManager -> {

            Role r = checkOrCreateRole(roleName);
            HashMap<String, Integer> actionsIds = new HashMap<>();
            HashMap<String, Permission> existingPermissions = new HashMap<>();

            //calculating pairs resourceName - actionsIds
            for (int i = 0; i < actions.size(); i++) {
                HyperIoTAction action = actions.get(i);
                //Checks if permission already exists for that resource
                Permission p = this.findByRoleAndResourceNameAndResourceIdInTransaction(r, action.getResourceName(), entityId);
                if (p == null)
                    getLog().debug("No permission found for resource: {} with id {} and role {}", new Object[]{action.getResourceName(),entityId, r.getName()});
                else
                    existingPermissions.put(action.getResourceName(), p);

                // create pair <resourceName, actionId> if resourceName does not exist, sum actionId otherwise
                actionsIds.merge(action.getResourceName(), action.getActionId(), Integer::sum);
            }
            checkOrCreatePermission(actionsIds,existingPermissions, r,entityId );

        });
    }

    @Override
    public boolean existPermissionSpecificToEntity(String resourceName, long resourceId) {
        if (resourceId == 0){
            return false;
        }
        return this.jpa.txExpr(TransactionType.Required, entityManager -> {
            String query = "SELECT COUNT(p) from Permission p where p.entityResourceName = :entityResourceName and p.resourceId = :resourceId";
            Number number = (Number) entityManager
                    .createQuery(query)
                    .setParameter("entityResourceName", resourceName)
                    .setParameter("resourceId", resourceId)
                    .getSingleResult();
            return number.longValue() > 0;
        });
    }

    /**
     This method works like findByRoleAndResourceNameAndResourceId but with the exception that
     the NoResultException is catch internally to the transaction such that transaction's rollback doesn't happen.
      Method is visibility is public such that the method is called in a transactional context.
      This method is used only for internal execution.
      This method isn't part of the OSGI service interface
     */
    public Permission findByRoleAndResourceNameAndResourceIdInTransaction(HyperIoTRole role,
                                                             String entityResourceName, long id) {
        getLog().debug("invoking findByRoleAndResourceNameAndResourceIdInTransaction Role: {}," +
                "entityResourceName {} , entityId {}", new Object[]{role, entityResourceName, id});
        return jpa.txExpr(TransactionType.Required, entityManager -> {
            Permission p = null;
            try{
                p=entityManager.createQuery(
                                "from Permission p where p.role.id = :roleId and p.entityResourceName = :entityResourceName and p.resourceId = :id",
                                Permission.class).setParameter("roleId", role.getId())
                        .setParameter("entityResourceName", entityResourceName).setParameter("id", id)
                        .getSingleResult();
            }catch (NoResultException e){
                getLog().debug(e.getMessage(), e);
            }
            return p;
        });
    }

    /**
     * Method is visibility is public such that the method is called in a transactional context.
     * This method is used only for internal execution.
     * This method isn't part of the OSGI service interface
     */
    public Role checkOrCreateRole(String roleName) {
        return jpa.txExpr(TransactionType.Required, entityManager -> {
            Role r = null;
            try {
                r = this.roleRepository.findByName(roleName);
            } catch (NoResultException e) {
                getLog().debug("No role found with name: {}", roleName);
            }

            if (r == null) {
                r = new Role();
                r.setName(roleName);
                r.setDescription("Role associated with the registered user");
                try {
                    entityManager.persist(r);
                } catch (Throwable t) {
                    getLog().error(t.getMessage(), t);
                }
            }
            return r ;
        });
    }

    /**
     * Method is visibility is public such that the method is called in a transactional context.
     * This method is used only for internal execution.
     * This method isn't part of the OSGI service interface
     */
    public void checkOrCreatePermission(HashMap<String, Integer> actionsIds,HashMap<String, Permission> existingPermissions, Role r,long entityId ){
        jpa.tx(TransactionType.Required, entityManager -> {
            // Save only modified permissions
            Iterator<String> it = actionsIds.keySet().iterator();
            while (it.hasNext()) {
                String resourceName = it.next();
                int actionIds = actionsIds.get(resourceName);
                Permission p = null;
                boolean mustUpdate = false;
                boolean isUnchanged = false;

                if (!existingPermissions.containsKey(resourceName)) {
                    // permission is new
                    p = new Permission();
                } else if (existingPermissions.get(resourceName).getActionIds() != actionIds) {
                    // permission has been modified (i.e. actions have been added or removed)
                    p = existingPermissions.get(resourceName);
                    mustUpdate = true;
                } else {
                    // permission has not been changed
                    isUnchanged = true;
                }

                if (!isUnchanged) {
                    // save or update
                    p.setEntityResourceName(resourceName);
                    p.setRole(r);
                    p.setActionIds(actionIds);
                    p.setName(resourceName + " " + r.getName() + " Permissions");
                    p.setResourceId(entityId);
                    try {
                        if (!mustUpdate)
                            entityManager.persist(p);
                        else
                            entityManager.merge(p);
                    } catch (Exception e) {
                        getLog().error(e.getMessage(), e);
                    }
                }
            }
        });

    }


}
