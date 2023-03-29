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

package it.acsoftware.hyperiot.role.repository;

import it.acsoftware.hyperiot.base.repository.HyperIoTBaseRepositoryImpl;
import it.acsoftware.hyperiot.role.api.RoleRepository;
import it.acsoftware.hyperiot.role.model.Role;
import org.apache.aries.jpa.template.JpaTemplate;
import org.apache.aries.jpa.template.TransactionType;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import javax.persistence.NoResultException;
import java.util.Collection;
import java.util.HashMap;
import java.util.Set;
import java.util.stream.Collectors;


/**
 * @author Aristide Cittadino Implementation class of the RoleRepository. This
 * class is used to interact with the persistence layer.
 */
@Component(service = RoleRepository.class, immediate = true)
public class RoleRepositoryImpl extends HyperIoTBaseRepositoryImpl<Role> implements RoleRepository {

    /**
     * Injecting the JpaTemplate to interact with database
     */
    private JpaTemplate jpa;

    /**
     * Constructor for a RoleRepositoryImpl
     */
    public RoleRepositoryImpl() {
        super(Role.class);
    }

    /**
     * @return The current jpa related to database operations
     */
    @Override
    protected JpaTemplate getJpa() {
        getLog().debug( "invoking getJpa, returning: {}" , jpa);
        return jpa;
    }

    /**
     * @param jpa Injection of JpaTemplate
     */
    @Override
    @Reference(target = "(osgi.unit.name=hyperiot-hUser-persistence-unit)")
    protected void setJpa(JpaTemplate jpa) {
        getLog().debug( "invoking setJpa, setting: {}" , jpa);
        this.jpa = jpa;
    }

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

    @Override
    public void remove(long id) {
        this.getJpa().tx(TransactionType.Required, entityManager -> {
            getLog().debug( "Transaction found, invoke persist");
            //Since it is an aggregate Role and permission should be in the same
            // Module, for now we execute ddelete query directly to avoid circular dependency
            // TODO: merge Permission and Role Modules into one aggregate
            entityManager
                .createQuery("delete Permission p where p.role.id = :roleId")
                .setParameter("roleId", id).executeUpdate();
            entityManager.flush();
            //Delete users association
            entityManager
                .createNativeQuery("delete from users_roles ur where ur.role_id = :roleId")
                .setParameter("roleId", id).executeUpdate();
            entityManager.flush();

            super.remove(id);
        });
    }

    /**
     * Collection of user roles obtained via query
     */
    public Collection<Role> getUserRoles(long userId) {
        getLog().debug( "invoking getUserRoles, by: {}", userId);
        return this.getJpa().txExpr(TransactionType.Required, entityManager -> {
            String query = "select u.roles from HUser u inner join u.roles where u.id=:userId";
            Collection<Role> userRole = entityManager.createQuery(query).setParameter("userId",userId).getResultList();
            return userRole.stream().collect(Collectors.toSet());

        });
    }

}
