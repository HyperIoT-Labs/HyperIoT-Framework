package it.acsoftware.hyperiot.authentication.service.jaas;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.model.authentication.context.HyperIoTContextFactory;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.api.RoleSystemApi;
import it.acsoftware.hyperiot.role.model.Role;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.apache.karaf.jaas.boot.principal.UserPrincipal;
import org.apache.karaf.jaas.modules.BackingEngine;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import java.security.AccessControlContext;
import java.security.AccessController;
import java.security.Principal;
import java.util.*;

@Component(service = BackingEngine.class)
public class HyperIoTJaaSBackingEngine implements BackingEngine {
    private static Logger log = LoggerFactory.getLogger(HyperIoTJaaSBackingEngine.class.getName());

    private HUserSystemApi gethUserSystemApi() {
        log.debug(
            "Invoking getHyperIoTAuthApi for searching for AuthenticationSystemApi");
        try {
            Collection<ServiceReference<HUserSystemApi>> references = HyperIoTUtil
                .getBundleContext(this)
                .getServiceReferences(HUserSystemApi.class, null);
            if (references != null && references.size() > 0) {
                return HyperIoTUtil.getBundleContext(this).getService(references.iterator().next());
            }
        } catch (InvalidSyntaxException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    private RoleSystemApi getRoleSystemApi() {
        log.debug(
            "Invoking getHyperIoTAuthApi for searching for AuthenticationSystemApi");
        try {
            Collection<ServiceReference<RoleSystemApi>> references = HyperIoTUtil
                .getBundleContext(this)
                .getServiceReferences(RoleSystemApi.class, null);
            if (references != null && references.size() > 0) {
                return HyperIoTUtil.getBundleContext(this).getService(references.iterator().next());
            }
        } catch (InvalidSyntaxException e) {
            log.warn(e.getMessage(), e);
        }
        return null;
    }

    @Override
    public void addUser(String username, String password) {
        log.debug("Adding user HyperIoT Backing engine");
        HUser user = new HUser();
        user.setAdmin(false);
        user.setEmail("createFromkaraf@hyperiot.com");
        user.setLastname("CreatedFromKaraf");
        user.setName("CreatedFromKaraf");
        user.setPassword(password);
        user.setPasswordConfirm(password);
        user.setUsername(username);
        this.gethUserSystemApi().save(user, getSecurityContext());
    }

    @Override
    public void deleteUser(String username) {
        log.debug("Delete user HyperIoT Backing engine");
        HUserSystemApi hUserSystemApi = this.gethUserSystemApi();
        HUser u = hUserSystemApi.findUserByUsername(username);
        hUserSystemApi.remove(u.getId(), this.getSecurityContext());
    }

    @Override
    public List<UserPrincipal> listUsers() {
        log.debug("List users HyperIoT Backing engine");
        Collection<HUser> users = this.gethUserSystemApi().findAll(null, this.getSecurityContext());
        List<UserPrincipal> principals = new ArrayList<>();
        for (HUser u : users) {
            principals.add(new UserPrincipal(u.getUsername()));
        }
        return principals;
    }

    @Override
    public UserPrincipal lookupUser(String username) {
        log.debug("Lookup user HyperIoT Backing engine");
        HUser u = this.gethUserSystemApi().findUserByUsername(username);
        if (u != null)
            return new UserPrincipal(u.getUsername());
        return null;
    }

    @Override
    public List<GroupPrincipal> listGroups(UserPrincipal user) {
        log.debug("List Groups HyperIoT Backing engine");
        return Collections.emptyList();
    }

    @Override
    public Map<GroupPrincipal, String> listGroups() {
        log.debug("List Groups HyperIoT Backing engine");
        return Collections.emptyMap();
    }

    @Override
    public void addGroup(String username, String group) {
        log.debug("Adding group HyperIoT Backing engine");
        throw new UnsupportedOperationException("Group are not managed at now!");
    }

    @Override
    public void createGroup(String group) {
        log.debug("Create group HyperIoT Backing engine");
        throw new UnsupportedOperationException("Group are not managed at now!");
    }

    @Override
    public void deleteGroup(String username, String group) {
        log.debug("Delete group HyperIoT Backing engine");
        throw new UnsupportedOperationException("Group are not managed at now!");
    }

    @Override
    public List<RolePrincipal> listRoles(Principal principal) {
        log.debug("List roles HyperIoT Backing engine");
        Collection<Role> roles = this.getRoleSystemApi().findAll(null, getSecurityContext());
        List<RolePrincipal> rolePs = new ArrayList<>();
        for (Role r : roles) {
            rolePs.add(new RolePrincipal(r.getName()));
        }
        return rolePs;
    }

    @Override
    public void addRole(String username, String role) {
        log.debug("Adding role HyperIoT Backing engine");
        Role r = this.getRoleSystemApi().findByName(role);
        HUserSystemApi hUserSystemApi = this.gethUserSystemApi();
        if (r != null) {
            HUser u = hUserSystemApi.findUserByUsername(username);
            if (!u.getRoles().contains(r)) {
                u.getRoles().add(r);
                hUserSystemApi.save(u, getSecurityContext());
            }
        }
    }

    @Override
    public void deleteRole(String username, String role) {
        log.debug("Delete role HyperIoT Backing engine");
        log.debug("Adding role HyperIoT Backing engine");
        Role r = this.getRoleSystemApi().findByName(role);
        if (r != null) {
            HUserSystemApi hUserSystemApi = this.gethUserSystemApi();
            HUser u = hUserSystemApi.findUserByUsername(username);
            if (u.getRoles().contains(r)) {
                u.getRoles().remove(r);
                hUserSystemApi.save(u, getSecurityContext());
            }
        }
    }

    @Override
    public void addGroupRole(String group, String role) {
        log.debug("Add group role HyperIoT Backing engine");
        throw new UnsupportedOperationException("Group are not managed at now!");
    }

    @Override
    public void deleteGroupRole(String group, String role) {
        log.debug("Delete group Role HyperIoT Backing engine");
        throw new UnsupportedOperationException("Group are not managed at now!");
    }

    protected HyperIoTContext getSecurityContext() {
        AccessControlContext acc = AccessController.getContext();
        Subject subject = Subject.getSubject(acc);
        Set<Principal> principals = subject.getPrincipals();
        if (principals.size() > 0) {
            return HyperIoTContextFactory.createBasicContext(principals);
        }
        return null;
    }


}
