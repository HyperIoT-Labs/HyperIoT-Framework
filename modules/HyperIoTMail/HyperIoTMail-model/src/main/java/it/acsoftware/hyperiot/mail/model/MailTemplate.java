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

package it.acsoftware.hyperiot.mail.model;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;

import it.acsoftware.hyperiot.base.validation.NotNullOnPersist;

import it.acsoftware.hyperiot.base.api.entity.HyperIoTProtectedEntity;
import it.acsoftware.hyperiot.base.model.HyperIoTAbstractEntity;
import it.acsoftware.hyperiot.base.validation.NoMalitiusCode;

/**
 *
 * @author Aristide Cittadino Model class for Mail of HyperIoT platform. This
 *         class is used to map Mail with the database.
 *
 */

@Entity
@Table(uniqueConstraints = { @UniqueConstraint(columnNames = "name") })
public class MailTemplate extends HyperIoTAbstractEntity implements HyperIoTProtectedEntity {

	private String name;
	private String description;
	private String content;

	@NotNullOnPersist
	@NoMalitiusCode
	@NotEmpty
	@Size( max = 255)
	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	@NotNullOnPersist
	@NoMalitiusCode
	@NotEmpty
    @Column(columnDefinition = "VARCHAR(3000)")
	public String getDescription() {
		return description;
	}

	public void setDescription(String description) {
		this.description = description;
	}

	@Column(columnDefinition = "TEXT")
	public String getContent() {
		return content;
	}

	public void setContent(String content) {
		this.content = content;
	}

}
