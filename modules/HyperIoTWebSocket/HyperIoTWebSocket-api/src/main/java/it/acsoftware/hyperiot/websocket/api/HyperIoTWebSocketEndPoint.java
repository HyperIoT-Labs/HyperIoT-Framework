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

package it.acsoftware.hyperiot.websocket.api;

import org.eclipse.jetty.websocket.api.Session;

import java.util.concurrent.Executor;

/**
 * Author Aristide Cittadino
 */
public interface HyperIoTWebSocketEndPoint {
    /**
     * @return Endpoint path
     */
    String getPath();

    /**
     * @param session
     * @return WebSocket Handler for specific Session
     */
    HyperIoTWebSocketSession getHandler(Session session);

    /**
     * This method should be used if you want to use different threads and executors with different types of opening connections
     * In order to avoid starvation.
     * Leave null for default Executor.
     *
     * @return
     */
    default Executor getExecutorForOpenConnections(Session s) {
        return null;
    }
}
