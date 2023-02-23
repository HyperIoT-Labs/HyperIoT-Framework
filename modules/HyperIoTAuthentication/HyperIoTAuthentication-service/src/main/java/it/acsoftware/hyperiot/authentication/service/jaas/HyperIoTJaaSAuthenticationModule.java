/*
 * Copyright 2019-2023 HyperIoT
 *
 * Licensed under the Apache License, Version 2.0 (the "License")
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package it.acsoftware.hyperiot.authentication.service.jaas;

import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.authentication.AuthenticationSystemApi;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.model.authentication.principal.HyperIoTGroupPrincipal;
import it.acsoftware.hyperiot.base.model.authentication.principal.HyperIoTPrincipal;
import it.acsoftware.hyperiot.base.model.authentication.principal.HyperIoTRolePrincipal;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;
import org.apache.karaf.jaas.boot.principal.RolePrincipal;
import org.osgi.framework.InvalidSyntaxException;
import org.osgi.framework.ServiceReference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.security.auth.Subject;
import javax.security.auth.callback.*;
import javax.security.auth.login.FailedLoginException;
import javax.security.auth.login.LoginException;
import javax.security.auth.spi.LoginModule;
import java.io.IOException;
import java.security.Principal;
import java.util.*;

/**
 * @author Aristide Cittadino Jaas Plugin for Authentication against HyperIoT
 * System.
 * Default Behaviour is only HUsers can login into the platform
 */
public class HyperIoTJaaSAuthenticationModule implements LoginModule {
    private static Logger log = LoggerFactory.getLogger(HyperIoTJaaSAuthenticationModule.class.getName());

    protected Subject subject;
    protected CallbackHandler callbackHandler;
    protected String user;
    protected HyperIoTAuthenticable loggedUser;
    protected final Set<Principal> principals = new HashSet<Principal>();
    protected boolean loginSucceeded;
    protected List<String> groups;
    protected Collection<? extends HyperIoTRole> roles;

    @Override
    public void initialize(Subject subject, CallbackHandler callbackHandler,
                           Map<String, ?> sharedState, Map<String, ?> options) {
        log.debug( "Initializing HyperIoT Authentication Module...");
        this.subject = subject;
        this.callbackHandler = callbackHandler;
        loginSucceeded = false;
        this.groups = new LinkedList<>();
        this.roles = new ArrayList<HyperIoTRole>();
    }


    @Override
    public boolean login() throws LoginException {
        log.debug( "Invoking HyperIoTJAAS Login...");
        Callback[] callbacks = new Callback[2];
        callbacks[0] = new NameCallback("Username: ");
        callbacks[1] = new PasswordCallback("Password: ", false);

        try {
            callbackHandler.handle(callbacks);
        } catch (IOException ioe) {
            throw new LoginException(ioe.getMessage());
        } catch (UnsupportedCallbackException uce) {
            throw new LoginException(
                    uce.getMessage() + " not available to obtain information from user");
        }

        user = ((NameCallback) callbacks[0]).getName();
        char[] tmpPassword = ((PasswordCallback) callbacks[1]).getPassword();

        if (tmpPassword == null) {
            tmpPassword = new char[0];
        }

        if (user == null) {
            throw new FailedLoginException("user name is null");
        }

        log.debug( "Login attemp with username: {}", user);
        if (!authenticate(user, String.valueOf(tmpPassword))) {
            throw new FailedLoginException("Authentication failed");
        }

        loginSucceeded = true;

        return loginSucceeded;
    }

    @Override
    public boolean commit() throws LoginException {
        boolean result = this.doCommit();
        if (result) {
            subject.getPrincipals().addAll(principals);
        }
        clear();
        return result;
    }

    protected boolean doCommit() throws LoginException {
        log.debug(
                "Committing login for user {} login successed: {}", new Object[]{user, loginSucceeded});
        boolean result = loginSucceeded;
        if (result) {
            log.debug( "Adding new Principal {}", this.loggedUser.toString());

            principals.add(new HyperIoTPrincipal(this.loggedUser.getScreenName(),
                    this.loggedUser.isAdmin()));

            for (String entry : groups) {
                log.debug( "Adding new Group Principal {}", entry);
                principals.add(new HyperIoTGroupPrincipal(entry));
            }

            for (HyperIoTRole role : roles) {
                principals.add(new HyperIoTRolePrincipal(role.getName()));
            }

            // admin user can connect to karaf via ssh
            // TO DO: We can define some permission on HUser entity
            if (loggedUser.isAdmin()) {
                principals.add(new RolePrincipal("ssh"));
                principals.add(new RolePrincipal("admin"));
                principals.add(new RolePrincipal("manager"));
                principals.add(new RolePrincipal("viewer"));
                principals.add(new GroupPrincipal("admingroup"));
                principals.add(new GroupPrincipal("systembundles"));
            }

        }
        return result;
    }

    @Override
    public boolean abort() throws LoginException {
        log.debug( "Aborting...");
        clear();
        return true;
    }

    @Override
    public boolean logout() throws LoginException {
        log.debug( "Logging out...");
        subject.getPrincipals().removeAll(principals);
        principals.clear();
        clear();
        return true;
    }

    private void clear() {
        user = null;
        loginSucceeded = false;
    }

    private boolean authenticate(String username, String password) {
        log.debug( "Invoke authentication ....");
        try {
            this.loggedUser = this.hyperIoTLogin(username, password);
        } catch (Exception e) {
            log.warn( "Error while logging in {}: {}", new Object[]{username, e.getMessage()});
        }
        if (loggedUser == null || !loggedUser.isActive())
            return false;
        roles = this.loggedUser.getRoles();
        this.postAuthentication(loggedUser);
        return true;
    }

    protected void postAuthentication(HyperIoTAuthenticable authenticated) {
        // DO NOTHING
    }

    protected HyperIoTAuthenticable hyperIoTLogin(String screename, String password) {
        return this.getAuthenticationApi().login(screename, password, this.getAuthenticationProviderFilter());
    }


    protected String getAuthenticationProviderFilter() {
        String osgiFilter = OSGiFilterBuilder.createFilter(HyperIoTConstants.OSGI_AUTH_PROVIDER_RESOURCE, HyperIoTUser.class.getName()).getFilter();
        return osgiFilter;
    }

    /**
     * return the current AuthenticationSystemService
     */
    private AuthenticationSystemApi getAuthenticationApi() {
        log.debug(
                "Invoking getHyperIoTAuthApi for searching for AuthenticationSystemApi");
        try {
            Collection<ServiceReference<AuthenticationSystemApi>> references = HyperIoTUtil
                    .getBundleContext(this)
                    .getServiceReferences(AuthenticationSystemApi.class, null);
            if (references != null && references.size() > 0) {
                return HyperIoTUtil.getBundleContext(this).getService(references.iterator().next());
            }
        } catch (InvalidSyntaxException e) {
            log.warn( e.getMessage(), e);
        }
        return null;
    }


}
