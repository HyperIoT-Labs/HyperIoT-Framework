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

package it.acsoftware.hyperiot.role.model;

import io.swagger.annotations.ApiModelProperty;
import it.acsoftware.hyperiot.base.api.HyperIoTRole;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;
import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;
import org.hibernate.validator.constraints.Length;

import javax.persistence.*;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

/**
 *
 * @author Aristide Cittadino Model class for Role of HyperIoT platform. This
 *         class is used to map Role with the database.
 *
 */
@Entity
@Table(uniqueConstraints = @UniqueConstraint(columnNames = "name"))
public class Role extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity, HyperIoTRole {

	/**
	 * String name for Role
	 */
	private String name;
	/**
	 * String description for Role
	 */
	private String description;

	/**
	 * Gets the role name
	 */
	@Column
	@NotNullOnPersist
	@NotEmpty
	@NoMalitiusCode
	@Size( max = 255)
	@ApiModelProperty(required = false)
	public String getName() {
		return name;
	}

	/**
	 * Sets the role name
	 */
	public void setName(String name) {
		this.name = name;
	}

	/**
	 * Gets the role description
	 *
	 * @return a string representing the role description
	 */
	@Column(length = 3000)
	@NotNullOnPersist
	@Size(max = 3000)
	@NoMalitiusCode
	@ApiModelProperty(required = false)
	public String getDescription() {
		return description;
	}

	/**
	 * Sets the role description
	 *
	 * @param description containing the role description
	 */
	public void setDescription(String description) {
		this.description = description;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
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
		Role other = (Role) obj;
		if (this.getId() > 0 && other.getId() == 0 || this.getId() == 0 && other.getId() > 0) {
			if (name == null) {
				if (other.name != null)
					return false;
			} else if (!name.equals(other.name))
				return false;
			return true;
		} else {
			return this.getId() == other.getId();
		}
	}

	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("Role Name: ").append(name);
		return sb.toString();
	}

}
