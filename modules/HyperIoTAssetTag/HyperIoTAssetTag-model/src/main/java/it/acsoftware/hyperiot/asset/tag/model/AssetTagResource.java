/*
 * Copyright 2019-2023 ACSoftware
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

package it.acsoftware.hyperiot.asset.tag.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;

import javax.persistence.Entity;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;

/**
 * @author Aristide Cittadino This class maps the concept of a resource (entity)
 * associated with a tag. This entity tracks association between
 * entities and tags in a generic way.
 */
@Entity
@Table(uniqueConstraints = {
        @UniqueConstraint(columnNames = {"resourceName", "resourceId", "tag_id"})})
public class AssetTagResource extends HyperIoTAbstractEntity
        implements HyperIoTProtectedEntity {
    /**
     * Resource name
     */
    @JsonProperty("resourceName")
    private String resourceName;
    /**
     * Resource id
     */
    private long resourceId;
    /**
     * Resource tag
     */
    private AssetTag tag;

    /**
     * @return resource name
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @NotBlank
    public String getResourceName() {
        return resourceName;
    }

    /**
     * @param resourceName resource name
     */
    public void setResourceName(String resourceName) {
        this.resourceName = resourceName;
    }

    /**
     * @return resource id
     */
    public long getResourceId() {
        return resourceId;
    }

    /**
     * @param resourceId resource Id
     */
    public void setResourceId(long resourceId) {
        this.resourceId = resourceId;
    }


    /**
     * @return tag Tag associated with the resource
     */
    @ManyToOne(targetEntity = AssetTag.class)
    public AssetTag getTag() {
        return tag;
    }

    /**
     * @param tag tag associated with the resource
     */
    public void setTag(AssetTag tag) {
        this.tag = tag;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((tag == null) ? 0 : tag.hashCode());
        result = prime * result + (int) (resourceId ^ (resourceId >>> 32));
        result = prime * result + ((resourceName == null) ? 0 : resourceName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        AssetTagResource other = (AssetTagResource) obj;
        if (other.getId() > 0 && this.getId() > 0)
            return other.getId() == this.getId();

        if (tag == null) {
            if (other.tag != null)
                return false;
        } else if (!tag.equals(other.tag))
            return false;
        if (resourceId != other.resourceId)
            return false;
        if (resourceName == null) {
            if (other.resourceName != null)
                return false;
        } else if (!resourceName.equals(other.resourceName))
            return false;
        return true;
    }
}
