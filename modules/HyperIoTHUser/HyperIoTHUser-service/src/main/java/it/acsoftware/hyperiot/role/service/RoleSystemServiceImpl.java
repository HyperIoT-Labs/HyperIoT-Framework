package it.acsoftware.hyperiot.role.service;

import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.util.Collection;


/**
 * @author Aristide Cittadino Implementation class of the RoleSystemApi
 * interface. This model class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = RoleSystemApi.class, immediate = true)
public final class RoleSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<Role>
    implements RoleSystemApi {

    /**
     * Injecting the RoleRepository to interact with persistence layer
     */
    private RoleRepository repository;

    /**
     * Constructor for a RoleSystemServiceImpl
     */
    public RoleSystemServiceImpl() {
        super(Role.class);
    }

    /**
     * Return the current repository
     */
    public RoleRepository getRepository() {
        getLog().debug("invoking getRepository, returning: {}", this.repository);
        return repository;
    }

    /**
     * @param roleRepository The current value to interact with persistence layer
     */
    @Reference
    protected void setRepository(RoleRepository roleRepository) {
        getLog().debug("invoking setRepository, setting: {}", roleRepository);
        this.repository = roleRepository;
    }

    /**
     * Collection of user roles obtained via query
     */
    public Collection<Role> getUserRoles(long userId) {
        return this.repository.getUserRoles(userId);
    }

    /**
     * @param name role Name
     * @return Role
     */
    @Override
    public Role findByName(String name) {
        return this.repository.findByName(name);
    }
}
