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

package it.acsoftware.hyperiot.base.model.authentication.principal;

import java.io.Serializable;
import java.security.Principal;
import org.apache.karaf.jaas.boot.principal.GroupPrincipal;

public class HyperIoTGroupPrincipal extends GroupPrincipal implements Principal, Serializable {
	/**
	 *
	 */
	private static final long serialVersionUID = 1L;

	public HyperIoTGroupPrincipal(String groupName) {
		super(groupName);
	}

}
