package it.acsoftware.hyperiot.shared.entity.model;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.acsoftware.hyperiot.base.api.HyperIoTUser;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTSharedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractResource;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;

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
