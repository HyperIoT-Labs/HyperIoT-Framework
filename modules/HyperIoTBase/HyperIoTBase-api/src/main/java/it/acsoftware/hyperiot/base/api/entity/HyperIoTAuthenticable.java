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

package it.acsoftware.hyperiot.base.api.entity;

import java.util.Collection;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;

/**
 * @author Aristide Cittadino This interface marks an entity to be
 * "authenticable" this means that it must expose methods for getting
 * and setting password and passwordConfirm fields.
 * <p>
 * This interface is useful if you want exploits password validation for
 * your entity. This enables sensor, devices, users to connect to
 * HyperIoT Platform.
 */
public interface HyperIoTAuthenticable extends HyperIoTBaseEntity {
    /**
     *
     * @return the field name which contains the screenName
     */
    @JsonIgnore
    String getScreenNameFieldName();

    /**
     * @return the username or thingname
     */
    String getScreenName();

    /**
     * @return true if it is an admin user
     */
    @JsonIgnore
    boolean isAdmin();

    /**
     * @return
     */
    Collection<? extends HyperIoTRole> getRoles();

    /**
     * @return the password
     */
    @JsonIgnore
    String getPassword();

    /**
     * @param password The confirm password
     */
    void setPassword(String password);

    /**
     * @return the confirm password
     */
    @JsonIgnore
    String getPasswordConfirm();

    /**
     * @param password the confirm password
     */
    void setPasswordConfirm(String password);

    /**
     * @return true if the authenticable is activated for authentication
     */
    @JsonIgnore
    boolean isActive();
}
