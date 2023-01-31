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

package it.acsoftware.hyperiot.websocket.channel.manager;

import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.osgi.util.filter.OSGiFilterBuilder;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannel;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterCoordinator;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelClusterMessageBroker;
import it.acsoftware.hyperiot.websocket.api.channel.HyperIoTWebSocketChannelManager;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketConstants;
import org.osgi.framework.ServiceReference;

public class HyperIoTWebSocketChannelManagerFactory {
    private HyperIoTWebSocketChannelClusterCoordinator coordinator;
    private HyperIoTWebSocketChannelClusterMessageBroker messageBroker;
    private Object channelType;

    public HyperIoTWebSocketChannelManagerFactory withClusterCoordinator(HyperIoTWebSocketChannelClusterCoordinator coordinator) {
        this.coordinator = coordinator;
        return this;
    }

    public HyperIoTWebSocketChannelManagerFactory withClusterMessageBroker(HyperIoTWebSocketChannelClusterMessageBroker broker) {
        this.messageBroker = broker;
        return this;
    }

    public HyperIoTWebSocketChannelManagerFactory loadFromOSGiContext(String clusterMessageBrokerOSGiName, String clusterCoordinatorOSGiName) {
        String clusterMessageBrokerFilter = OSGiFilterBuilder.createFilter(HyperIoTWebSocketConstants.CHANNEL_CLUSTER_MESSAGE_BROKER_OSGI_FILTER_NAME, clusterMessageBrokerOSGiName).getFilter();
        String clusterCoordinatorFilter = OSGiFilterBuilder.createFilter(HyperIoTWebSocketConstants.CHANNEL_CLUSTER_COORDINATOR_OSGI_FILTER_NAME, clusterCoordinatorOSGiName).getFilter();

        ServiceReference[] messageBrokerRefs = HyperIoTUtil.getServices(HyperIoTWebSocketChannelClusterMessageBroker.class, clusterMessageBrokerFilter);
        ServiceReference[] clusterCoordinatorRefs = HyperIoTUtil.getServices(HyperIoTWebSocketChannelClusterCoordinator.class, clusterCoordinatorFilter);
        //loading cluster message broker from registered components
        if (messageBrokerRefs != null && messageBrokerRefs.length > 0) {
            this.withClusterMessageBroker((HyperIoTWebSocketChannelClusterMessageBroker) messageBrokerRefs[0].getBundle().getBundleContext().getService(messageBrokerRefs[0]));
        }

        if (clusterCoordinatorRefs != null && clusterCoordinatorRefs.length > 0) {
            this.withClusterCoordinator((HyperIoTWebSocketChannelClusterCoordinator) clusterCoordinatorRefs[0].getBundle().getBundleContext().getService(clusterCoordinatorRefs[0]));
        }

        return this;
    }

    public <T extends HyperIoTWebSocketChannel> HyperIoTWebSocketChannelManager build(Class<T> channelType) {
        if (channelType == null)
            throw new HyperIoTRuntimeException("Channel Manager must be set with a channel type, please use withChannelType method");
        HyperIoTWebSocketChannelManager manager = new HyperIoTWebSocketDefaultChannelManager(channelType, this.coordinator, this.messageBroker);
        return manager;
    }

    public void reset() {
        this.coordinator = null;
        this.messageBroker = null;
    }

    public static HyperIoTWebSocketChannelManagerFactory newDefaultChannelManagerFactory() {
        return new HyperIoTWebSocketChannelManagerFactory();
    }

}
