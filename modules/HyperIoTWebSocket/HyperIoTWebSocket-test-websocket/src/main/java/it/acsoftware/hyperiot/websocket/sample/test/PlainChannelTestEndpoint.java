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

package it.acsoftware.hyperiot.websocket.sample.test;

import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketEndPoint;
import it.acsoftware.hyperiot.websocket.api.HyperIoTWebSocketSession;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.channel.HyperIoTWebSocketBasicChannel;
import it.acsoftware.hyperiot.websocket.channel.manager.HyperIoTWebSocketChannelManagerFactory;
import it.acsoftware.hyperiot.websocket.channel.session.HyperIoTWebSocketChannelBasicSession;
import org.eclipse.jetty.websocket.api.Session;
import org.osgi.service.component.annotations.Component;

import java.util.concurrent.Executor;

@Component(service = HyperIoTWebSocketEndPoint.class, immediate = true)
public class PlainChannelTestEndpoint implements HyperIoTWebSocketEndPoint {
    private static HyperIoTWebSocketChannelManager manager;

    @Override
    public String getPath() {
        return "test-channel";
    }

    @Override
    public HyperIoTWebSocketSession getHandler(Session session) {
        //using basic session with no authentication
        return new HyperIoTWebSocketChannelBasicSession(session, false, getChannelManager());
    }

    @Override
    public Executor getExecutorForOpenConnections(Session s) {
        return HyperIoTWebSocketEndPoint.super.getExecutorForOpenConnections(s);
    }

    /**
     * Channel manager must be singleton inside each node.
     * @return
     */
    private static HyperIoTWebSocketChannelManager getChannelManager() {
        if (manager == null) {
            manager = HyperIoTWebSocketChannelManagerFactory.newDefaultChannelManagerFactory()
                    .loadFromOSGiContext("kafka-cluster-message-broker", "zookeeper-cluster-coordinator")
                    //channel class
                    .build(HyperIoTWebSocketBasicChannel.class);
        }
        return manager;
    }
}
