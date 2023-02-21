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
