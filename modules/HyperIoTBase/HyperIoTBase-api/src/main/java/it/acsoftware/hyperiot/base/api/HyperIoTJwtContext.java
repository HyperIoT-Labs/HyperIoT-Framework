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

package it.acsoftware.hyperiot.base.api;

import org.apache.cxf.rs.security.jose.jwt.JwtToken;
import org.apache.cxf.security.SecurityContext;

public interface HyperIoTJwtContext extends HyperIoTContext, SecurityContext {

    /**
     * Returns jwt token
     */
    JwtToken getAuthenticationToken();

    /**
     * Returns a boolean value indicating that the logged user is included in a
     * role.
     */
    boolean loggedUserHasRole(String role);

    /**
     * @return the Java Type of the logged user since the platform can support multiple logged in entities
     */
    Class getIssuerType();
}
