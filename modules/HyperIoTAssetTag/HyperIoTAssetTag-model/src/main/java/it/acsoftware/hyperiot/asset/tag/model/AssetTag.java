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

import com.fasterxml.jackson.annotation.JsonIgnore;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAssetOwnerImpl;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;


import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.HashSet;
import java.util.Set;

/**
 * @author Aristide Cittadino Model class for AssetTag of HyperIoT
 * platform. This class maps the concept of Tag that can be
 * associated with any entity inside HyperIoT
 */
@Entity
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"name", "ownerresourcename", "ownerresourceid"})})
public class AssetTag extends HyperIoTAbstractEntity
        implements HyperIoTProtectedEntity {

    /**
     * Name
     */
    private String name;

    /**
     * Who owns the tag
     */
    private HyperIoTAssetOwnerImpl owner;

    /**
     * Resources associated with the current tag
     */
    private Set<AssetTagResource> resources;

    private String description;
    private String color;

    public AssetTag() {
        super();
        this.resources = new HashSet<>();
    }

    /**
     * @return category name
     */
    @NotNullOnPersist
    @NotEmpty
    @NoMalitiusCode
    @Size( max = 255)
    public String getName() {
        return name;
    }

    /**
     * @param name
     */
    public void setName(String name) {
        this.name = name;
    }

    /**
     * @return
     */
    @NotNullOnPersist
    public HyperIoTAssetOwnerImpl getOwner() {
        return owner;
    }

    /**
     * @param owner
     */
    public void setOwner(HyperIoTAssetOwnerImpl owner) {
        this.owner = owner;
    }

    /**
     * @return resources associated with the current category
     */
    @JsonIgnore
    @OneToMany(mappedBy = "tag", orphanRemoval = true, cascade = CascadeType.ALL)
    public Set<AssetTagResource> getResources() {
        return resources;
    }

    /**
     * @param resources resources to be set on current category
     */
    public void setResources(Set<AssetTagResource> resources) {
        this.resources = resources;
    }

    @NoMalitiusCode
    @Size( max = 255)
    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @NoMalitiusCode
    @Size(min = 3, max = 7)
    public String getColor() {
        return color;
    }

    public void setColor(String color) {
        this.color = color;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((owner == null) ? 0 : owner.hashCode());
        result = prime * result + ((name == null) ? 0 : name.hashCode());
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
        AssetTag other = (AssetTag) obj;

        if (other.getId() > 0 && this.getId() > 0)
            return other.getId() == this.getId();

        if (owner == null) {
            if (other.owner != null)
                return false;
        } else if (!owner.equals(other.owner))
            return false;
        if (name == null) {
            if (other.name != null)
                return false;
        } else if (!name.equals(other.name))
            return false;
        return true;
    }
}