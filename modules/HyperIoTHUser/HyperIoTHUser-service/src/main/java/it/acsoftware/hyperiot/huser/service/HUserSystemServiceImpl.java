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

package it.acsoftware.hyperiot.huser.service;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTUserNotActivated;
import it.acsoftware.hyperiot.base.service.entity.HyperIoTBaseEntitySystemServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserRepository;
import it.acsoftware.hyperiot.huser.api.HUserSystemApi;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.model.Role;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;


/**
 * @author Aristide Cittadino Implementation class of the HUserSystemApi
 * interface. This model class is used to implement all additional
 * methods to interact with the persistence layer.
 */
@Component(service = HUserSystemApi.class, immediate = true)
public final class HUserSystemServiceImpl extends HyperIoTBaseEntitySystemServiceImpl<HUser>
    implements HUserSystemApi {


    /**
     * Injecting the HUserRepository to interact with persistence layer
     */
    private HUserRepository repository;

    /**
     * Constructor for a HUserSystemServiceImpl
     */
    public HUserSystemServiceImpl() {
        super(HUser.class);
    }

    /**
     * Return the current repository
     */
    protected HUserRepository getRepository() {
        getLog().debug( "invoking getRepository, returning: {}", this.repository);
        return repository;
    }

    /**
     * @param hUserRepository The current value to interact with persistence layer
     */
    @Reference
    protected void setRepository(HUserRepository hUserRepository) {
        getLog().debug( "invoking setRepository, setting: {}", hUserRepository);
        this.repository = hUserRepository;
    }

    /**
     * This method allows to find an existing user by username
     *
     * @param username parameter required to find a user
     * @return the user researched
     */
    public HUser findUserByUsername(String username) {
        return this.repository.findByUsername(username);
    }

    /**
     * @param username
     * @param password
     * @return
     */
    @Override
    public HUser login(String username, String password) {
        HUser user = null;
        try {
            user = this.findUserByUsername(username);
            //try to find by mail
        } catch (NoResultException e) {
            getLog().debug( "trying to login with email");
        }
        try {
            if (user == null) {
                user = this.findUserByEmail(username);
            }
            if (user != null) {
                if (!user.isActive())
                    throw new HyperIoTUserNotActivated();
                if (HyperIoTUtil.passwordMatches(password, user.getPassword())) {
                    return user;
                }
            }
        } catch (NoResultException e) {
            getLog().debug( "No User found with specified username: {}", username);
        }
        return null;
    }

    /**
     * This method allows to find an existing user by email
     *
     * @param email parameter required to find a user
     * @return the user researched
     */
    public HUser findUserByEmail(String email) {
        return this.repository.findByEmail(email);
    }

    /**
     * This method allows a user registration that will be accessible by
     * unregistered users
     *
     * @param u   parameter required to register a user
     * @param ctx user context of HyperIoT platform
     */
    public void registerUser(HUser u, HyperIoTContext ctx) {
        getLog().debug( "invoking registerUser, User: {}", new Object[]{u, ctx});
        this.save(u, ctx);
    }

    /**
     * Password cannot be changed by this method
     */
    @Override
    public HUser update(HUser u, HyperIoTContext ctx) {
        HUser dbUser = this.find(u.getId(), ctx);
        if (dbUser == null)
            throw new HyperIoTEntityNotFound();
        u.setPassword(dbUser.getPassword());
        u.setPasswordConfirm(null);
        u.setDeletionCode(dbUser.getDeletionCode());
        u.setEntityVersion(dbUser.getEntityVersion());
        return super.update(u, ctx);
    }

    @Override
    public HUser changePassword(HUser user, String newPassword, String passwordConfirm) {
        if (user == null)
            throw new HyperIoTEntityNotFound();
        user.setPassword(newPassword);
        user.setPasswordConfirm(passwordConfirm);
        this.validate(user);
        return this.repository.changePassword(user, newPassword, passwordConfirm);
    }

    @Override
    public HUser changeDeletionCode(HyperIoTContext ctx, String deletionCode) {
        HUser dbUser = this.find(ctx.getLoggedEntityId(), null);
        if (dbUser == null)
            throw new HyperIoTEntityNotFound();
        String encodedDeletionCode = HyperIoTUtil.encodeRawString(deletionCode);
        dbUser.setDeletionCode(encodedDeletionCode);
        return super.update(dbUser, ctx);
    }

    @Override
    public void activateUser(String email, String activationCode) {
        this.repository.activateUser(email, activationCode);
    }

    /**
     * This method checks if a user is not an admin and assigns him an administrator
     * role
     */
    @Activate
    public void checkHAdminExists() {
        HUser admin = repository.findHAdmin();
        if (admin == null) {
            admin = new HUser();
            admin.setAdmin(true);
            admin.setActive(true);
            admin.setEmail("hadmin@hyperiot.com");
            admin.setLastname("Admin");
            admin.setName("Admin");
            String password = "admin";
            admin.setPassword(password);
            admin.setPasswordConfirm(password);
            admin.setUsername("hadmin");
            repository.save(admin);
        }
    }

    @Override
    public boolean screeNameAlreadyExists(HyperIoTAuthenticable hyperIoTAuthenticable) {
        return this.repository.screeNameAlreadyExists(hyperIoTAuthenticable);
    }
}
