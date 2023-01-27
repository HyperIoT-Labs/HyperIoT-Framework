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

package it.acsoftware.hyperiot.websocket.test.client;

import org.eclipse.jetty.websocket.api.Session;
import org.eclipse.jetty.websocket.api.annotations.*;

import java.util.concurrent.ThreadPoolExecutor;

@WebSocket
public class HyperIoTChannelWebSocket {
    private ThreadPoolExecutor executor;
    private HyperIoTChannelParticipant participant;

    public HyperIoTChannelWebSocket(HyperIoTChannelParticipant participant, ThreadPoolExecutor executor) {
        this.executor = executor;
        this.participant = participant;
    }

    @OnWebSocketConnect
    public void onOpen(Session session) {
        this.executor.execute(() -> this.participant.onConnected(session));
    }

    @OnWebSocketMessage
    public void onMessage(Session s, String message) {
        this.executor.execute(() -> this.participant.onMessage(s, message));
    }

    @OnWebSocketError
    public void error(Session s, Throwable error) {
        this.executor.execute(() -> this.participant.onError(s, error));
    }

    @OnWebSocketClose
    public void close(Session s, int status, String reason) {
        System.out.println("CLOSING: " + reason);
    }

    public void shutdown() {
        this.executor.shutdown();
    }
}
