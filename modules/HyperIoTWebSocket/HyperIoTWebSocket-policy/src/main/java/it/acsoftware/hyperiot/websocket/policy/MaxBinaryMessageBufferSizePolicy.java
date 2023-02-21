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

package it.acsoftware.hyperiot.websocket.policy;

import org.eclipse.jetty.websocket.api.Session;

import java.util.Map;

public class MaxBinaryMessageBufferSizePolicy extends HyperIoTWebSocketAbstractPolicy  {
    private int maxBinaryMessageBufferSize;

    public MaxBinaryMessageBufferSizePolicy(Session s, int maxBinaryMessageBufferSize) {
        super(s);
        this.maxBinaryMessageBufferSize = maxBinaryMessageBufferSize;
    }

    public int getMaxBinaryMessageBufferSize() {
        return maxBinaryMessageBufferSize;
    }

    @Override
    public boolean isSatisfied(Map<String, Object> params, byte[] payload) {
        return true;
    }

    @Override
    public boolean closeWebSocketOnFail() {
        return false;
    }

    @Override
    public boolean printWarningOnFail() {
        return false;
    }

    @Override
    public boolean sendWarningBackToClientOnFail() {
        return false;
    }

    @Override
    public boolean ignoreMessageOnFail() {
        return false;
    }
}
