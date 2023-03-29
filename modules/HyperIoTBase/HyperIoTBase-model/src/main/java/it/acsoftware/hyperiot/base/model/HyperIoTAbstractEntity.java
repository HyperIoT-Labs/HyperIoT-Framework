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

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonView;
import it.acsoftware.hyperiot.base.api.HyperIoTOwnedResource;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTBaseEntity;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import javax.persistence.*;
import javax.validation.constraints.NotNull;
import java.util.Date;

/**
 * @author Aristide Cittadino Model class for HyperIoTAbstractEntity. This class
 * implements HyperIoTBaseEntity methods and maps primary key of entity
 * in the database.
 */
@MappedSuperclass
@Embeddable
public abstract class HyperIoTAbstractEntity extends HyperIoTAbstractResource
        implements HyperIoTBaseEntity {

    /**
     * long id, indicates primary key of entity
     */
    private long id;

    /**
     * Version identifier for optimistic locking
     */
    private int entityVersion = 1;

    /**
     * Auto filled: create date
     */
    private Date entityCreateDate;

    /**
     * Auto filled: update date
     */
    private Date entityModifyDate;

    private long[] categoryIds;

    private long[] tagIds;

    /**
     * Gets the primary key of entity
     */
    @Id
    @GeneratedValue
    @NotNullOnPersist
    @JsonView({HyperIoTJSONView.Public.class,HyperIoTJSONView.Compact.class,HyperIoTJSONView.Extended.class})
    public long getId() {
        return id;
    }

    /**
     * Sets the primary key of entity
     */
    public void setId(long id) {
        this.id = id;
    }

    /**
     * @return get date of entity creation
     */
    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entity_create_date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Date getEntityCreateDate() {
        return entityCreateDate;
    }

    /**
     * @param createDate
     */
    public void setEntityCreateDate(Date createDate) {
        this.entityCreateDate = createDate;
    }

    /**
     * @return the date od the entity update
     */
    @UpdateTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @Column(name = "entity_modify_date")
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    public Date getEntityModifyDate() {
        return entityModifyDate;
    }

    /**
     * @param modifyDate
     */
    public void setEntityModifyDate(Date modifyDate) {
        this.entityModifyDate = modifyDate;
    }

    /**
     * @return Version management for optimistic locking
     */
    @Version
    @NotNull
    @Column(name="entity_version",columnDefinition = "INTEGER default 1")
    @JsonView(HyperIoTJSONView.Public.class)
    public int getEntityVersion() {
        return entityVersion;
    }

    /**
     * @param version
     */
    public void setEntityVersion(int version) {
        this.entityVersion = version;
    }

    /**
     * Not persistend on database used to reference categories with the
     * HyperIoTAssetCategoryManager
     *
     * @return categoryIds
     */
    @Transient
    @JsonView(HyperIoTJSONView.Public.class)
    public long[] getCategoryIds() {
        return categoryIds;
    }

    /**
     * @param categoryIds category Ids
     */
    public void setCategoryIds(long[] categoryIds) {
        this.categoryIds = categoryIds;
    }

    /**
     * Not persisted on database used to reference tags with the
     * HyperIoTAssetTagManager
     *
     * @return categoryIds
     */
    @Transient
    @JsonView(HyperIoTJSONView.Public.class)
    public long[] getTagIds() {
        return tagIds;
    }

    /**
     * @param tagIds tagIds
     */
    public void setTagIds(long[] tagIds) {
        this.tagIds = tagIds;
    }

    /*@Override
    @Transient
    @JsonIgnore
    public HyperIoTBaseEntity getParent() {
        return null;
    }*/

    @Override
    @Transient
    @JsonIgnore
    public String getSystemApiClassName() {
        String className = this.getClass().getName();
        return className.replace(".model.", ".api.") + "SystemApi";
    }

}
