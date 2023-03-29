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

package it.acsoftware.hyperiot.base.api;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;

/**
 * @author Aristide Cittadino Generic interface component for HyperIoTRole. This
 * interface defines methods for obtaining and setting the name of a
 * protected role in the HyperIoT platform.
 */
public interface HyperIoTRole extends HyperIoTProtectedEntity {

    /**
     * Gets a name of protected Role
     *
     * @return name of protected Role
     */
    public String getName();

    /**
     * Sets a name of protected Role
     *
     * @param name sets a name of protected Role
     */
    public void setName(String name);

    /**
     * @return Role descriptiono
     */
    public String getDescription();

    /**
     * @param description Role description
     */
    public void setDescription(String description);

}
