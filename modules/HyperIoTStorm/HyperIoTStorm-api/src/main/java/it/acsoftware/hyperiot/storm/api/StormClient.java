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

package it.acsoftware.hyperiot.storm.api;

import org.apache.storm.generated.StormTopology;
import org.apache.storm.generated.TopologyInfo;
import org.apache.storm.generated.TopologySummary;
import org.apache.storm.thrift.TException;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.List;

/**
 * 
 * @author Aristide Cittadino Interface component for StormClientApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface StormClient {

    public List<TopologySummary> getTopologyList() throws TException;

    /**
     * Gets the topology instance object.
     *
     * @param topologyId Topology ID
     * @return StormTopology object
     * @throws TException
     */
    public StormTopology getTopology(String topologyId) throws TException;

    /**
     * Gets the topology configuration.
     *
     * @param topologyId Topology ID
     * @return The JSON serialized topology configuration
     * @throws TException
     */
    public String getTopologyConfig(String topologyId) throws TException;

    /**
     * Gets the topology info.
     *
     * @param topologyId Topology ID
     * @return TopologyInfo object
     * @throws TException
     */
    public TopologyInfo getTopologyInfo(String topologyId) throws TException;

    /**
     * Activates the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void activate(String topologyName) throws TException;

    /**
     * Deactivates the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void deactivate(String topologyName) throws TException;

    /**
     * Kills the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void killTopology(String topologyName) throws TException;

    /**
     * Returns the hashcode of the topology configuration.
     * Needed to undestand if the current config has changed
     * compared to the older one
     * @param summary
     * @return
     */
    public int getTopologyConfigHashCode(TopologySummary summary);

    /**
     * Submits a topology with initial INACTIVE status.
     *
     * @param topologyProperties Topology properties text
     * @param topologyYaml       Topology YAML definition text
     * @return The submitted topology name
     * @throws IOException
     * @throws IllegalAccessException
     * @throws InvocationTargetException
     * @throws InstantiationException
     * @throws NoSuchFieldException
     * @throws NoSuchMethodException
     * @throws ClassNotFoundException
     * @throws org.apache.storm.generated.InvalidTopologyException
     * @throws org.apache.storm.generated.AuthorizationException
     * @throws org.apache.storm.generated.AlreadyAliveException
     */
    public String submitTopology(String topologyProperties, String topologyYaml,int topologyConfigHashCode)
            throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, TException;

}