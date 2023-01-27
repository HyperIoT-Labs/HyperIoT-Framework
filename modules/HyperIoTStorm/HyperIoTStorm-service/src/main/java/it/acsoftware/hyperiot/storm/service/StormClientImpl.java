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

package it.acsoftware.hyperiot.storm.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.storm.api.StormClient;
import it.acsoftware.hyperiot.storm.api.StormTopologyBuilder;
import org.apache.storm.Config;
import org.apache.storm.StormSubmitter;
import org.apache.storm.flux.FluxBuilder;
import org.apache.storm.flux.model.TopologyDef;
import org.apache.storm.flux.parser.FluxParser;
import org.apache.storm.generated.*;
import org.apache.storm.thrift.TException;
import org.apache.storm.utils.NimbusClient;
import org.apache.storm.utils.Utils;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;

import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.lang.reflect.InvocationTargetException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;


/**
 * 
 * @author Aristide Cittadino Implementation class of StormClientApi interface.
 *         It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(immediate = true, service = StormClient.class)
public final class StormClientImpl  implements StormClient {


    public static final long STORM_CLIENT_RETRY_TIMEOUT = 2000;
    public static final int STORM_CLIENT_MAX_RETRIES = 5;
    public static final String HYPERIOT_PROPERTY_STORM_NIMBUS_SEEDS = "it.acsoftware.hyperiot.stormmanager.nimbus.seeds";
    public static final String HYPERIOT_STORM_TOPOLOGY_PROP_HASHCODE = "it.acsoftware.hyperiot.storm.topology.hashcode";
    public static final String HYPERIOT_PROPERTY_STORM_TOPOLOGY_JAR_NAME = "it.acsoftware.hyperiot.stormmanager.topology.jar";
    public static final String HYPERIOT_PROPERTY_STORM_TOPOLOGY_DIR = "it.acsoftware.hyperiot.stormmanager.topology.dir";

    public static final ObjectMapper mapper = new ObjectMapper();
    private static final Logger log = LoggerFactory.getLogger(StormClientImpl.class.getName());
    private Map stormConfig;
    private final boolean dumpYaml = true;
    private final boolean envFilter = true;
    private String topologyPath;
    private NimbusClient nimbusClient;

    private StormTopologyBuilder stormTopologyBuilder ;

    @Reference
    public void setStormTopologyBuilder(StormTopologyBuilder stormTopologyBuilder){
        log.debug("invoking setStormTopologyBuilder, setting: {}" , stormTopologyBuilder);
        this.stormTopologyBuilder = stormTopologyBuilder;
    }

    public StormTopologyBuilder getStormTopologyBuilder(){
        log.debug("invoking getStormTopologyBuilder, returning: {}" , this.stormTopologyBuilder);
        return stormTopologyBuilder;
    }


    @Activate
    public void onActivate(BundleContext ctx) {
        String topologyJarName = (String) HyperIoTUtil.getHyperIoTProperty(HYPERIOT_PROPERTY_STORM_TOPOLOGY_JAR_NAME);
        String topologyDir = (String) HyperIoTUtil.getHyperIoTProperty(HYPERIOT_PROPERTY_STORM_TOPOLOGY_DIR);
        this.topologyPath = topologyDir + topologyJarName + ".jar";
    }

    private Nimbus.Client getClient() {
        if (this.nimbusClient == null)
            this.configureClient();
        return getClientOrRetry(STORM_CLIENT_MAX_RETRIES);
    }

    private synchronized Nimbus.Client getClientOrRetry(int retries) {
        try {
            Nimbus.Client client = (Nimbus.Client) nimbusClient.getClient();
            //verify connection
            verifyClientConnection(client);
            return client;
        } catch (Throwable t) {
            log.error(t.getMessage(), t);
            retries--;
            if (retries >= 0) {
                try {
                    Thread.sleep(STORM_CLIENT_RETRY_TIMEOUT);
                } catch (InterruptedException e) {
                    log.error(e.getMessage(), e);
                }
                this.configureClient();
                //forcing client reconfiguration
                return getClientOrRetry(retries);
            } else {
                throw new HyperIoTRuntimeException(t.getMessage());
            }
        }
    }

    private void verifyClientConnection(Nimbus.Client client) throws TException {
        client.getClusterInfo();
    }

    private void configureClient() {
        List<String> nimbusSeeds = Arrays.asList(((String) HyperIoTUtil.getHyperIoTProperty(HYPERIOT_PROPERTY_STORM_NIMBUS_SEEDS)).split(","));
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        try {
            ClassLoader stormCoreClassLoader = StormTopology.class.getClassLoader();
            Thread.currentThread().setContextClassLoader(stormCoreClassLoader);
            stormConfig = Utils.readStormConfig();
            stormConfig.put(Config.NIMBUS_SEEDS, nimbusSeeds);
            nimbusClient = NimbusClient.getConfiguredClient(stormConfig);
            log.info("Storm Client connected!");
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            nimbusClient = null;
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
    }

    private void setClassLoader() {
        ClassLoader stormCoreClassLoader = StormTopology.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(stormCoreClassLoader);
    }

    /**
     * Gets the list of alive topologies.
     *
     * @return List of TopologySummary
     * @throws TException
     */
    public List<TopologySummary> getTopologyList() throws TException {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        setClassLoader();
        String errorMessage = null;
        List<TopologySummary> topologySummaries = null;
        try {
            topologySummaries = getClient().getClusterInfo().get_topologies();
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error(errorMessage, e);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
        if (errorMessage != null)
            throw new TException(errorMessage);
        return topologySummaries;
    }

    /**
     * Gets the topology instance object.
     *
     * @param topologyId Topology ID
     * @return StormTopology object
     * @throws TException
     */
    public StormTopology getTopology(String topologyId) throws TException {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        setClassLoader();
        String errorMessage = null;
        StormTopology stormTopology = null;
        try {
            stormTopology = getClient().getTopology(topologyId);
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error(errorMessage, e);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
        if (errorMessage != null)
            throw new TException(errorMessage);
        return stormTopology;
    }

    /**
     * Gets the topology configuration.
     *
     * @param topologyId Topology ID
     * @return The JSON serialized topology configuration
     * @throws TException
     */
    public String getTopologyConfig(String topologyId) throws TException {
        return getClient().getTopologyConf(topologyId);
    }

    /**
     * Gets the topology info.
     *
     * @param topologyId Topology ID
     * @return TopologyInfo object
     * @throws TException
     */
    public TopologyInfo getTopologyInfo(String topologyId) throws TException {
        return getClient().getTopologyInfo(topologyId);
    }

    /**
     * Activates the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void activate(String topologyName) throws TException {
        getClient().activate(topologyName);
    }

    /**
     * Deactivates the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void deactivate(String topologyName) throws TException {
        getClient().deactivate(topologyName);
    }

    /**
     * Kills the topology with the given name.
     *
     * @param topologyName Topology name
     * @throws TException
     */
    public void killTopology(String topologyName) throws TException {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        setClassLoader();
        String errorMessage = null;
        try {
            KillOptions killOpts = new KillOptions();
            killOpts.set_wait_secs(0); // time to wait before killing
            getClient().killTopologyWithOpts(topologyName, killOpts); //provide topology name
        } catch (Exception e) {
            errorMessage = e.getMessage();
            log.error(errorMessage, e);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
        if (errorMessage != null)
            throw new TException(errorMessage);
    }

    public int getTopologyConfigHashCode(TopologySummary summary) {
        if (summary != null) {
            try {
                String conf = this.getTopologyConfig(summary.get_id());
                Map<String, Object> topologyConfig = mapper.readValue(conf, Map.class);
                return (Integer) topologyConfig.get(HYPERIOT_STORM_TOPOLOGY_PROP_HASHCODE);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }
        }
        return -1;
    }

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
     * @throws InvalidTopologyException
     * @throws AuthorizationException
     * @throws AlreadyAliveException
     */
    public synchronized String submitTopology(String topologyProperties, String topologyYaml, int topologyConfigHashCode)
            throws IOException, IllegalAccessException, InvocationTargetException, InstantiationException, NoSuchFieldException, NoSuchMethodException, ClassNotFoundException, TException {
        // store topology property and yaml files in a temporary folder
        String randomUUIDString = UUID.randomUUID().toString();
        Path topologyPropsPath = writeTempFile(randomUUIDString, ".properties", topologyProperties.getBytes());
        Properties filterProps = FluxParser.parseProperties(
                topologyPropsPath.toAbsolutePath().toString(),
                false
        );
        topologyPropsPath.toFile().delete();
        Path topologyYamlPath = writeTempFile(randomUUIDString, ".yaml", topologyYaml.getBytes());
        TopologyDef topologyDef = FluxParser.parseFile(
                topologyYamlPath.toAbsolutePath().toString(),
                dumpYaml,
                true,
                filterProps, envFilter
        );
        topologyYamlPath.toFile().delete();
        String topologyName = topologyDef.getName();
        // merge contents of `config` into topology config
        Config conf = new Config();
        conf.putAll(stormConfig);
        conf.putAll(FluxBuilder.buildConfig(topologyDef));
        conf.put(HYPERIOT_STORM_TOPOLOGY_PROP_HASHCODE, topologyConfigHashCode);
        // Kills topology `topologyName` before submitting if already exists
        try {
            killTopology(topologyName);
            Thread.sleep(1000);
        } catch (Exception e) {
            log.warn(e.getMessage(), e);
        }
        SubmitOptions submitOptions = new SubmitOptions(TopologyInitialStatus.ACTIVE);
        // submit the topology
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader stormClassLoader = StormTopology.class.getClassLoader();
        System.setProperty("storm.jar", this.topologyPath);
        Thread.currentThread().setContextClassLoader(stormClassLoader);
        // Create context and topology instance
        try {
            StormTopology topology = this.stormTopologyBuilder.configureTopology(conf);
            StormSubmitter.submitTopology(topologyName, conf, topology, submitOptions, null);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
        return topologyName;
    }

    private Path writeTempFile(String fileName, String fileExtension, byte[] fileContent) throws IOException {
        final Path path = Files.createTempFile(fileName, fileExtension);
        //Writing data here
        byte[] buf = fileContent;
        Files.write(path, buf);
        //Delete file on application exit
        if (path.toFile().exists())
            path.toFile().deleteOnExit();
        return path;
    }

}
