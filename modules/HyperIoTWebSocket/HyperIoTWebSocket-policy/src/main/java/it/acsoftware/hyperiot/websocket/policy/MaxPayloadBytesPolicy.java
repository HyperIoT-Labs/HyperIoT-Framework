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

package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MaxPayloadBytesPolicy extends HyperIoTWebSocketAbstractPolicy {
    private static Logger log = LoggerFactory.getLogger(MaxPayloadBytesPolicy.class.getName());
    private int maxPayloadBytes;


    public MaxPayloadBytesPolicy(Session s, int maxPayloadBytes) {
        super(s);
        this.maxPayloadBytes = maxPayloadBytes;
    }

    @Override
    public boolean closeWebSocketOnFail() {
        return false;
    }

    @Override
    public boolean printWarningOnFail() {
        return true;
    }

    @Override
    public boolean sendWarningBackToClientOnFail() {
        return true;
    }

    @Override
    public boolean ignoreMessageOnFail() {
        return true;
    }

    @Override
    public boolean isSatisfied(Map<String, Object> params, byte[] payload) {
        log.debug( "Policy Max Payload bytes, current payload is: {}, max is {}", new Object[]{payload.length, maxPayloadBytes});
        if (payload.length > maxPayloadBytes) {
            return false;
        }
        return true;
    }
}
