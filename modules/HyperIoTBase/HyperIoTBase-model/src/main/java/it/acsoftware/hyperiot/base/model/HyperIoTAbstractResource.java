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

package it.acsoftware.hyperiot.base.model;

import javax.persistence.Transient;

import com.fasterxml.jackson.annotation.JsonIgnore;
import io.swagger.annotations.ApiModelProperty;
import it.acsoftware.hyperiot.base.api.HyperIoTResource;

/**
 * @author Aristide Cittadino Model class for HyperIoTAbstractResource. This
 * class implements HyperIoTResource methods to obtain the resource name
 * of the entity mapped to the database.
 */
public abstract class HyperIoTAbstractResource implements HyperIoTResource {

    /**
     * Gets the resource name of entity of HyperIoT platform
     */
    @Override
    @Transient
    @JsonIgnore
    @ApiModelProperty(hidden = true)
    public String getResourceName() {
        return this.getClass().getName();
    }
}
