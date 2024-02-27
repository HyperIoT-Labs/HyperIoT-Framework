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

package it.acsoftware.hyperiot.shared.entity.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractResource;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import java.io.Serializable;
import java.util.Date;
import java.util.Objects;

/**
 * @author Aristide Cittadino Model class for SharedEntity of HyperIoT platform. This
 * class is used to map SharedEntity with the database.
 */
@Entity
//@Table annotation is needed for custom check for duplicate base on this composite primary key
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"entityResourceName", "entityId", "userId"})})
@IdClass(SharedEntity.SharedEntityPK.class)
public class SharedEntity extends HyperIoTAbstractResource implements HyperIoTProtectedEntity {
    private String entityResourceName;
    private long entityId;
    private long userId;
    private String userEmail;
    private String username;

    @Id
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    public String getEntityResourceName() {
        return entityResourceName;
    }

    public void setEntityResourceName(String entityResourceName) {
        this.entityResourceName = entityResourceName;
    }

    @Id
    @NotNullOnPersist
    public long getEntityId() {
        return entityId;
    }

    public void setEntityId(long entityId) {
        this.entityId = entityId;
    }

    @Id
    @NotNullOnPersist
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    @Override
    @Transient
    @JsonIgnore
    public long getId() {
        return 0;
    }

    @Override
    public void setId(long l) {
    }

    @Override
    @Transient
    @JsonIgnore
    public long[] getCategoryIds() {
        return new long[0];
    }

    @Override
    public void setCategoryIds(long[] longs) {

    }

    @Override
    @Transient
    @JsonIgnore
    public Date getEntityCreateDate() {
        return null;
    }

    @Override
    public void setEntityCreateDate(Date date) {

    }

    @Override
    @Transient
    @JsonIgnore
    public int getEntityVersion() {
        return 0;
    }

    @Override
    public void setEntityVersion(int i) {

    }

    @Override
    @Transient
    @JsonIgnore
    public long[] getTagIds() {
        return new long[0];
    }

    @Override
    public void setTagIds(long[] longs) {

    }

    @Transient
    @JsonIgnore
    public String getSystemApiClassName() {
        String className = this.getClass().getName();
        return className.replace(".model.", ".api.") + "SystemApi";
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getUserEmail() {
        return userEmail;
    }

    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    @Transient
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public static class SharedEntityPK implements Serializable {
        private String entityResourceName;
        private long entityId;
        private long userId;


        public SharedEntityPK() {
        }

        public SharedEntityPK(String entityResourceName, long entityId, long userId) {
            this.entityResourceName = entityResourceName;
            this.entityId = entityId;
            this.userId = userId;
        }

        public String getEntityResourceName() {
            return entityResourceName;
        }

        public void setEntityResourceName(String entityResourceName) {
            this.entityResourceName = entityResourceName;
        }

        public long getEntityId() {
            return entityId;
        }

        public void setEntityId(long entityId) {
            this.entityId = entityId;
        }

        public long getUserId() {
            return userId;
        }

        public void setUserId(long userId) {
            this.userId = userId;
        }


        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            SharedEntityPK that = (SharedEntityPK) o;
            return entityId == that.entityId &&
                userId == that.userId &&
                entityResourceName.equals(that.entityResourceName);
        }

        @Override
        public int hashCode() {
            return Objects.hash(entityResourceName, entityId, userId);
        }

        public String toString() {
            return entityResourceName + ", " + entityId + ", " + userId;
        }
    }
}
