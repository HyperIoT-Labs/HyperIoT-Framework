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

package it.acsoftware.hyperiot.permission.model;

import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import it.acsoftware.hyperiot.huser.model.HUser;
import it.acsoftware.hyperiot.role.model.Role;

import javax.persistence.*;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Positive;
import javax.validation.constraints.Size;

/**
 * @author Aristide Cittadino Model class for Permission of HyperIoT platform.
 * It is used to map Permission with the database.
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = {"role_id","huser_id", "entityResourceName", "resourceId"}))
public class Permission extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {

    /**
     * String name for Permission
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private String name;
    /**
     * int actionIds for Permission
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private int actionIds;
    /**
     * String entityResourceName for Permission
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private String entityResourceName;
    /**
     * long resourceId for Permission
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private Long resourceId;

    /**
     * Role role for Permission. It is used to map the element in the @ManyToOne
     * association.
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private Role role;

    /**
     * Permissions can be related to roles or directly to users
     */
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class})
    private HUser huser;

    /**
     * Gets the Permission name
     *
     * @return String that represents Permission name
     */
    @Column
    @NotBlank
    @NoMalitiusCode
    @Size( max = 255)
    public String getName() {
        return this.name;
    }

    /**
     * Sets the Permission name
     *
     * @param name sets the Permission name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * Gets the Permission actionIds
     *
     * @return the value of Permission actionIds
     */
    @Column
    @Positive
    public int getActionIds() {
        return actionIds;
    }

    /**
     * Sets the Permission actionIds
     *
     * @param actionIds sets the permission action id
     */
    public void setActionIds(int actionIds) {
        this.actionIds = actionIds;
    }

    /**
     * Gets the Permission entityResourceName
     *
     * @return String that represents Permission resource name
     */
    @Column
    @NotNullOnPersist
    @NotEmpty
    @Size (max = 255)
    @NoMalitiusCode
    public String getEntityResourceName() {
        return entityResourceName;
    }

    /**
     * Sets the Permission entityResourceName
     *
     * @param entityResourceName String that sets the Permission resource name
     */
    public void setEntityResourceName(String entityResourceName) {
        this.entityResourceName = entityResourceName;
    }

    /**
     * Gets the Permission resourceId
     *
     * @return the value of Permission resource id
     */
    @Column
    public Long getResourceId() {
        return resourceId;
    }

    /**
     * Sets the Permission resourceId
     *
     * @param resourceId sets the permission resource id
     */
    public void setResourceId(Long resourceId) {
        this.resourceId = resourceId;
    }

    /**
     * Gets the Permission Role
     *
     * @return the value that represents the permission role
     */
    @ManyToOne(targetEntity = Role.class)
    public Role getRole() {
        return role;
    }

    /**
     * Sets the Permission Role
     *
     * @param role value that sets the permission role
     */
    public void setRole(Role role) {
        this.role = role;
    }

    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }

    @ManyToOne
    public HUser getHuser() {
        return huser;
    }

    public void setHuser(HUser huser) {
        this.huser = huser;
    }

    /**
     * Adds the related action id (if not contained already) to the permission
     *
     * @param action
     */
    public void addPermission(HyperIoTAction action) {
        if ((this.actionIds & action.getActionId()) != action.getActionId())
            this.actionIds += action.getActionId();
    }

    /**
     * Removes the related action id (if contained ) from the permission
     *
     * @param action
     */
    public void removePermission(HyperIoTAction action) {
        if ((this.actionIds & action.getActionId()) == action.getActionId())
            this.actionIds -= action.getActionId();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null) {
            return false;
        }
        if (!(obj instanceof Permission)) {
            return false;
        }
        Permission permission = (Permission) obj;
        if ((this.getId() == 0 && permission.getId() != 0) || (this.getId() != 0 && permission.getId() == 0)) {
            return this.getName().equals(permission.getName())
                    && this.getEntityResourceName().equals(permission.getEntityResourceName())
                    && this.getActionIds() == permission.getActionIds();
        } else {
            return this.getId() == permission.getId();
        }
    }

    @Override
    public int hashCode() {
        int result = 3;
        result = 11 * result + getName().hashCode();
        result = 11 * result + getEntityResourceName().hashCode();
        return result;
    }

    @Override
    public String toString() {
        return "Permission [id=" + this.getId() + ",\t name=" + name + ",\t resourceName=" + this.getEntityResourceName()
                + ",\t actionIds=" + actionIds + "]";
    }

}
