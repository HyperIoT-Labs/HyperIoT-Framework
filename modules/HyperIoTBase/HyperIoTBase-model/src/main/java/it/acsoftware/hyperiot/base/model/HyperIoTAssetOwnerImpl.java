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

import javax.persistence.Embeddable;

@Embeddable
public class HyperIoTAssetOwnerImpl /*implements HyperIoTAssetOwner*/ {
    private String ownerResourceName;
    private Long ownerResourceId;
    private long userId;

    //	@Override
    public String getResourceName() {
        return this.getOwnerResourceName();
    }

    public void setResourceName(String name) {
        return;
    }

    //	@Override
    public long getUserId() {
        return userId;
    }

    public void setUserId(long userId) {
        this.userId = userId;
    }

    public String getOwnerResourceName() {
        return ownerResourceName;
    }

    public void setOwnerResourceName(String ownerResourceName) {
        this.ownerResourceName = ownerResourceName;
    }

    public Long getOwnerResourceId() {
        return ownerResourceId;
    }

    public void setOwnerResourceId(Long ownerResourceId) {
        this.ownerResourceId = ownerResourceId;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((ownerResourceId == null) ? 0 : ownerResourceId.hashCode());
        result = prime * result + ((ownerResourceName == null) ? 0 : ownerResourceName.hashCode());
        result = prime * result + (int) (userId ^ (userId >>> 32));
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
        HyperIoTAssetOwnerImpl other = (HyperIoTAssetOwnerImpl) obj;

        if (ownerResourceId == null) {
            if (other.ownerResourceId != null)
                return false;
        } else if (!ownerResourceId.equals(other.ownerResourceId))
            return false;
        if (ownerResourceName == null) {
            if (other.ownerResourceName != null)
                return false;
        } else if (!ownerResourceName.equals(other.ownerResourceName))
            return false;
        if (userId != other.userId)
            return false;
        return true;
    }

}
