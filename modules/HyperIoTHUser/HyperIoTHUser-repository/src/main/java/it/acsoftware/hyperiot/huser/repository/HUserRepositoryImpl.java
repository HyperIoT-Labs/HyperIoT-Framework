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

package it.acsoftware.hyperiot.huser.repository;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.exception.HyperIoTEntityNotFound;
import it.acsoftware.hyperiot.base.exception.HyperIoTUserAlreadyActivated;
import it.acsoftware.hyperiot.base.exception.HyperIoTWrongUserActivationCode;
import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.huser.api.HUserRepository;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.model.Role;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.HashMap;


/**
 * @author Aristide Cittadino Implementation class of the HUserRepository. It is
 * used to interact with the persistence layer.
 */
@Component(service = HUserRepository.class, immediate = true)
public class HUserRepositoryImpl extends HyperIoTBaseRepositoryImpl<HUser> implements HUserRepository {
    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Injecting role Repository
     */
    private RoleRepository roleRepository;

    /**
     * Constructor for HUserRepositoryImpl
     */
    public HUserRepositoryImpl() {
        super(HUser.class);
    }

    /**
     * @return The current jpa is related to database operations
     */
    @Override
    protected JpaTemplate getJpa() {
        getLog().debug("invoking getJpa, returning: {}", jpa);
        return jpa;
    }

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Override
    @Reference(target = "(osgi.unit.name=hyperiot-hUser-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug("invoking setJpa, setting: {}", jpa);
        this.jpa = jpa;
    }

    /**
     * @param roleRepository Injecting RoleRepository
     */
    @Reference
    public void setRoleRepository(RoleRepository roleRepository) {
        this.roleRepository = roleRepository;
    }

    /**
     * Find a user with admin role via query
     *
     * @return the user with admin role
     */
    @Override
    public HUser findHAdmin() {
        getLog().debug("Invoking findHAdmin ");
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            getLog().debug("Transaction found, invoke persist");
            HUser entity = null;
            try {
                entity = entityManager.createQuery("from HUser h where h.username = 'hadmin'", HUser.class)
                    .getSingleResult();
                getLog().debug("Entity persisted: " + entity);
            } catch (NoResultException e) {
                getLog().debug("Entity NOT FOUND ");
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
            return entity;
        });
    }

    /**
     * Find an existing user by username via query
     *
     * @param username parameter required to find an existing user
     * @return the user with username entered
     */
    public HUser findByUsername(String username) {
        getLog().debug("Repository findByUsername {}", username);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            getLog().debug("Transaction found, invoke findByUsername");
            HUser user = null;
            try {
                user = entityManager
                    .createQuery("from HUser h left join fetch h.roles where lower(h.username)=lower(:username) ", HUser.class)
                    .setParameter("username", username).getSingleResult();
                getLog().debug("Query results: {}", user);
            } catch (NoResultException e) {
                getLog().debug("Entity Not Found ");
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
            return user;
        });
    }

    @Override
    public HUser findByEmail(String email) {
        getLog().debug("Repository findByEmail {}", email);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            getLog().debug("Transaction found, invoke findByEmail");
            HUser user = null;
            try {
                user = entityManager
                    .createQuery("from HUser h left join fetch h.roles where h.email=:email ", HUser.class)
                    .setParameter("email", email).getSingleResult();
                getLog().debug("Query results: {}", user);
            } catch (NoResultException e) {
                getLog().debug("Entity Not Found ");
            } catch (Exception e) {
                getLog().error(e.getMessage(), e);
            }
            return user;
        });
    }

    /**
     * Activates and adds RegisteredUser Role to the activated user
     *
     * @param email          Email for activation
     * @param activationCode Activation Code
     */

    public void activateUser(String email, String activationCode) {
        this.getJpa().tx(TransactionType.Required, entityManger -> {
            HUser user = findByEmail(email);
            Role registeredUser = roleRepository.findByName(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER);
            if (user != null) {
                if (user.isActive()) {
                    throw new HyperIoTUserAlreadyActivated();
                }

                if (user.getActivateCode().equals(activationCode)) {
                    user.setActive(true);
                    user.setActivateCode(null);
                    //adding registered user role
                    user.addRole(registeredUser);
                    entityManger.persist(user);
                } else {
                    throw new HyperIoTWrongUserActivationCode();
                }
            } else {
                throw new HyperIoTEntityNotFound();
            }
        });
    }

    /**
     * Forcing Password Hash
     */
    @Override
    public HUser save(HUser entity) {
        String password = entity.getPassword();
        String passwordMD5 = HyperIoTUtil.getPasswordHash(password);
        entity.setPassword(passwordMD5);
        entity.setPasswordConfirm(passwordMD5);
        entity.setDeletionCode(null);
        return super.save(entity);
    }


    @Override
    public HUser changePassword(HUser user, String newPassword, String passwordConfirm) {
        if(user == null ){
            throw new HyperIoTEntityNotFound();
        }
        String newPasswordEncoded = HyperIoTUtil.getPasswordHash(newPassword);
        user.setPassword(newPasswordEncoded);
        user.setPasswordConfirm(newPasswordEncoded);
        //forcing password reset code to be empty on each pwd change
        user.setPasswordResetCode(null);
        super.update(user);
        return user;
    }


    /**
     *
     * @param hyperIoTAuthenticable
     * @return
     */
    @Override
    public boolean screeNameAlreadyExists(HyperIoTAuthenticable hyperIoTAuthenticable) {
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
        try {
            String username = hyperIoTAuthenticable.getScreenName();
            String query = "from HUser u where lower(u.username) = lower(:username)";
            HUser huser = entityManager.createQuery(query,HUser.class).setParameter("username",username).getSingleResult();
            if (hyperIoTAuthenticable instanceof HUser) {
                HUser huserAuthenticable = (HUser) hyperIoTAuthenticable;
                return !huserAuthenticable.equals(huser);
            }
            return huser != null;
        } catch (NoResultException e) {
            getLog().debug("No devices with device name: {}", hyperIoTAuthenticable.getScreenName());
        }
        return false;
        });
    }
}
