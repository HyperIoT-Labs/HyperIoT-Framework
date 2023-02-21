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

package it.acsoftware.hyperiot.role.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseRepository;
import it.acsoftware.hyperiot.role.model.Role;

import java.util.Collection;

/**
 * @author Aristide Cittadino Interface component for Role Repository.
 * RoleRepository is used for CRUD operations,
 * and to interact with the persistence layer.
 */
public interface RoleRepository extends HyperIoTBaseRepository<Role> {
    /**
     * @param name role Name
     * @return Role
     */
    public Role findByName(String name);

    /**
     * Collection of user roles obtained via query
     */
    public Collection<Role> getUserRoles(long userId);
}
