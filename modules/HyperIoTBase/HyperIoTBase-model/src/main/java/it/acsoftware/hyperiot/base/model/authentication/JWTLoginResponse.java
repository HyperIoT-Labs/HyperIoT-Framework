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

package it.acsoftware.hyperiot.base.model.authentication;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.api.entity.HyperIoTAuthenticable;
import it.acsoftware.hyperiot.base.model.HyperIoTJSONView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;

/**
 * @author Aristide Cittadino Model class for JWT Token
 */
public class JWTLoginResponse {
    private static Logger log = LoggerFactory.getLogger(JWTLoginResponse.class);
    private static ObjectMapper mapper = new ObjectMapper();
    /**
     * Serialized and encrypted string for JWT Token
     */
    private String token;
    /**
     * User info
     */
    private HyperIoTAuthenticable authenticable;

    /**
     * HashMap which stores resourcenames and profiles, so permissions
     */
    private HashMap<String, JWTProfile> profile;


    public JWTLoginResponse(String token, HyperIoTAuthenticable authenticable) {
        super();
        this.token = token;
        this.profile = new HashMap<>();
        this.authenticable = authenticable;
    }

    /**
     * @return the encoded Token
     */
    public String getToken() {
        return token;
    }

    public HashMap<String, JWTProfile> getProfile() {
        return profile;
    }

    public HyperIoTAuthenticable getAuthenticable() {
        return authenticable;
    }

    public void setAuthenticable(HyperIoTAuthenticable authenticable) {
        this.authenticable = authenticable;
    }

    public String toJson() {
        try {
            return mapper.writerWithView(HyperIoTJSONView.Public.class).writeValueAsString(this);
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
        }
        return null;
    }

}
