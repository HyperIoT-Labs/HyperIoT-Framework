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

package it.acsoftware.hyperiot.zookeeper.connector.service;

import it.acsoftware.hyperiot.base.api.HyperIoTLeadershipRegistrar;
import it.acsoftware.hyperiot.base.exception.HyperIoTRuntimeException;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.base.util.HyperIoTConstants;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorSystemApi;
import it.acsoftware.hyperiot.zookeeper.connector.model.HyperIoTZooKeeperData;
import it.acsoftware.hyperiot.zookeeper.connector.util.HyperIoTZookeeperConstants;
import org.apache.curator.RetryPolicy;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.recipes.leader.LeaderLatch;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.curator.framework.recipes.locks.InterProcessMutex;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.zookeeper.CreateMode;
import org.apache.zookeeper.data.Stat;
import org.osgi.framework.BundleContext;
import org.osgi.framework.ServiceReference;
import org.osgi.service.cm.Configuration;
import org.osgi.service.cm.ConfigurationAdmin;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.util.tracker.ServiceTracker;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * @author Aristide Cittadino Implementation class of the ZookeeperConnectorSystemApi
 * interface. This  class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = ZookeeperConnectorSystemApi.class, immediate = true)
public final class ZookeeperConnectorSystemServiceImpl extends HyperIoTBaseSystemServiceImpl implements ZookeeperConnectorSystemApi {
    private CuratorFramework client;
    private Properties zkProperties;
    private HashMap<String, LeaderLatch> leaderSelectorsMap;
    private ServiceTracker<HyperIoTLeadershipRegistrar, HyperIoTLeadershipRegistrar> leadershipRegistrarServiceTracker;
    private Map<String, InterProcessMutex> interProcessMutex;

    public ZookeeperConnectorSystemServiceImpl() {
        getLog().debug("Creating service for ZookeeperConnectorSystemApi");
        leaderSelectorsMap = new HashMap<>();
        interProcessMutex = new HashMap<>();
    }

    /**
     * On Activation, zookeeper module loads the configuration, connects to zookeeper and tries to write
     * info about the current container in which it's executed
     */
    @Activate
    public void activate(BundleContext bundleContext) {
        getLog().debug("Activating bundle Zookeeper Connector System API");
        loadZookeeperConfig();
        createZookeeperConnection();
        defineLeadershipRegistrar(bundleContext);
    }

    private void createZookeeperConnection() {
        RetryPolicy retryPolicy = new ExponentialBackoffRetry(1000, 10);
        Object zkConn = zkProperties.getOrDefault(HyperIoTZookeeperConstants.ZOOKEEPER_CONNECTION_URL, "localhost:2181");
        getLog().debug("Connecting to zookeeper {}", new Object[]{zkConn});
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                ZookeeperConnectorSystemServiceImpl.this.client = CuratorFrameworkFactory.newClient((String) (zkProperties.getOrDefault(HyperIoTZookeeperConstants.ZOOKEEPER_CONNECTION_URL, "localhost:2181")), retryPolicy);
                client.start();
                ZookeeperConnectorSystemServiceImpl.this.registerContainerInfo();
            }
        });
        t.start();
    }

    private void defineLeadershipRegistrar(BundleContext bundleContext) {
        if (leadershipRegistrarServiceTracker == null) {
            // When called for the first time, create a new service tracker
            // that tracks the availability of a HyperIoTLeadershipRegistrar service.
            leadershipRegistrarServiceTracker = new ServiceTracker<HyperIoTLeadershipRegistrar, HyperIoTLeadershipRegistrar>(
                    bundleContext, HyperIoTLeadershipRegistrar.class, null) {

                // This method is invoked when a service (of the kind tracked) is added
                @Override
                public HyperIoTLeadershipRegistrar addingService(ServiceReference<HyperIoTLeadershipRegistrar> reference) {
                    HyperIoTLeadershipRegistrar result = super.addingService(reference);
                    registerLeadershipComponent(result);
                    return result;
                }

                // This method is invoked when a service is removed
                @Override
                public void removedService(ServiceReference<HyperIoTLeadershipRegistrar> reference,
                                           HyperIoTLeadershipRegistrar service) {
                    unregisterLeadershipComponent(service);
                    super.removedService(reference, service);
                }
            };
        }
        // Now activate (open) the service tracker.
        leadershipRegistrarServiceTracker.open();
    }

    @Deactivate
    public void deactivate() {
        getLog().debug("Disconnecting from Zookeeper....");
        this.client.close();
        leadershipRegistrarServiceTracker.close();
    }

    /**
     * Writes to zookeeper current container info
     */
    private void registerContainerInfo() {
        try {
            HyperIoTZooKeeperData data = new HyperIoTZooKeeperData();
            String nodeId = HyperIoTUtil.getNodeId();
            String layer = HyperIoTUtil.getLayer();
            String path = HyperIoTZookeeperConstants.HYPERIOT_ZOOKEEPER_BASE_PATH + "/" + layer + "/" + nodeId;
            data.addParam("nodeId", nodeId.getBytes(StandardCharsets.UTF_8));
            data.addParam("layer", layer.getBytes(StandardCharsets.UTF_8));
            getLog().debug("Registering Container info on zookeeper with nodeId: {} layer: {} data: \n {}", new Object[]{nodeId, layer, new String(data.getBytes())});
            this.create(CreateMode.EPHEMERAL, path, data.getBytes(), true);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    private void registerLeadershipComponent(HyperIoTLeadershipRegistrar component) {
        String zkPath = HyperIoTZookeeperConstants.HYPERIOT_ZOOKEEPER_BASE_PATH + component.getLeadershipPath();
        try {
            this.startLeaderLatch(zkPath);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    private void unregisterLeadershipComponent(HyperIoTLeadershipRegistrar component) {
        String zkPath = HyperIoTZookeeperConstants.HYPERIOT_ZOOKEEPER_BASE_PATH + component.getLeadershipPath();
        try {
            this.closeLeaderLatch(zkPath);
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        }
    }

    /**
     * Loads zookeeper config
     */
    private void loadZookeeperConfig() {
        getLog().debug("Zookeeper Properties not cached, reading from .cfg file...");
        BundleContext context = HyperIoTUtil.getBundleContext(this.getClass());
        ServiceReference<?> configurationAdminReference = context
                .getServiceReference(ConfigurationAdmin.class.getName());

        if (configurationAdminReference != null) {
            ConfigurationAdmin confAdmin = (ConfigurationAdmin) context
                    .getService(configurationAdminReference);
            try {
                Configuration configuration = confAdmin.getConfiguration(
                        HyperIoTConstants.HYPERIOT_ZOOKEEPER_CONNECTOR_CONFIG_FILE_NAME);
                if (configuration != null && configuration.getProperties() != null) {
                    getLog().debug("Reading properties for Zookeeper....");
                    Dictionary<String, Object> dict = configuration.getProperties();
                    List<String> keys = Collections.list(dict.keys());
                    Map<String, Object> dictCopy = keys.stream()
                            .collect(Collectors.toMap(Function.identity(), dict::get));
                    zkProperties = new Properties();
                    zkProperties.putAll(dictCopy);
                    getLog().debug("Loaded properties For Zookeeper: {}", zkProperties);
                }
            } catch (Exception e) {
                getLog().error(
                        "Impossible to find {}, please create it!", new Object[]{HyperIoTConstants.HYPERIOT_ZOOKEEPER_CONNECTOR_CONFIG_FILE_NAME, e});
            }
        }
    }

    /**
     * @param mode
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @Override
    public void create(CreateMode mode, String path, byte[] data, boolean createParentFolders) throws Exception {
        if (createParentFolders)
            this.client.create().creatingParentsIfNeeded().withMode(mode).forPath(path, data);
        else
            this.client.create().withMode(mode).forPath(path, data);
    }

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @Override
    public void createEphemeral(String path, byte[] data, boolean createParentFolders) throws Exception {
        CreateMode mode = CreateMode.EPHEMERAL;
        this.create(mode, path, data, createParentFolders);
    }

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @Override
    public void createPersistent(String path, byte[] data, boolean createParentFolders) throws Exception {
        CreateMode mode = CreateMode.PERSISTENT;
        this.create(mode, path, data, createParentFolders);
    }

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    @Override
    public void create(String path, byte[] data, boolean createParentFolders) throws Exception {
        if (createParentFolders)
            this.client.create().creatingParentsIfNeeded().forPath(path, data);
        else
            this.client.create().forPath(path);
    }

    /**
     * @param path
     * @throws Exception
     */
    @Override
    public void delete(String path) throws Exception {
        this.client.delete().quietly().deletingChildrenIfNeeded().forPath(path);
    }

    /**
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public byte[] read(String path) throws Exception {;
        return read(path,false);
    }

    @Override
    public byte[] read(String path,boolean lock) throws Exception {
        if (path.endsWith("/") || path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);
        if(lock)
            lock(path);
        byte[] data = this.client.getData().forPath(path);
        if(lock)
            unlock(path);
        return data;
    }

    /**
     * @param path
     * @return
     * @throws Exception
     */
    @Override
    public void update(String path, byte[] data) throws Exception {
        if (path.endsWith("/") || path.endsWith("\\"))
            path = path.substring(0, path.length() - 1);
        lock(path);
        this.client.setData().forPath(path, data);
        unlock(path);
    }

    @Override
    public boolean checkExists(String path) throws Exception {
        Stat stats = this.client.checkExists().forPath(path);
        return stats != null;
    }

    @Override
    public void addListener(LeaderLatchListener listener, String leadershipPath) {
        String zkPath = HyperIoTZookeeperConstants.HYPERIOT_ZOOKEEPER_BASE_PATH + leadershipPath;
        getLog().info("Adding listener to LeaderLatch on zkPath " + zkPath);
        if (leaderSelectorsMap.containsKey(zkPath))
            leaderSelectorsMap.get(zkPath).addListener(listener);
        else
            getLog().warn("Could not add listener: LeaderLatch does not exist on zkPath " + zkPath);
    }

    /**
     * @param mutexPath zkNode path
     * @return True if a LeaderLatch exists on path and it has leadership, false otherwise
     */
    public boolean isLeader(String mutexPath) {
        String zkPath = HyperIoTZookeeperConstants.HYPERIOT_ZOOKEEPER_BASE_PATH + mutexPath;
        if (leaderSelectorsMap.containsKey(zkPath))
            return leaderSelectorsMap.get(zkPath).hasLeadership();
        return false;
    }

    @Override
    public CuratorFramework getZookeeperCuratorClient() {
        return this.client;
    }

    /**
     * @param mutexPath MutexPath
     * @throws Exception Exception
     */
    public void closeLeaderLatch(String mutexPath) throws Exception {
        if (leaderSelectorsMap.containsKey(mutexPath)) {
            LeaderLatch ll = leaderSelectorsMap.remove(mutexPath);
            ll.close();
        }
    }

    /**
     * @param mutexPath
     * @throws Exception
     */
    private void startLeaderLatch(String mutexPath) throws Exception {
        LeaderLatch ll = this.getOrCreateLeaderLatch(mutexPath);
        ll.start();
    }

    /**
     * @param mutexPath
     * @return
     */
    private LeaderLatch getOrCreateLeaderLatch(String mutexPath) {
        LeaderLatch ll = null;
        if (!leaderSelectorsMap.containsKey(mutexPath)) {
            ll = new LeaderLatch(this.client, mutexPath, HyperIoTUtil.getNodeId());
            leaderSelectorsMap.put(mutexPath, ll);
        } else {
            ll = leaderSelectorsMap.get(mutexPath);
        }
        return ll;
    }

    private void lock(String path) throws Exception {
        interProcessMutex.computeIfAbsent(path, key -> new InterProcessMutex(client, path));
        interProcessMutex.get(path).acquire();
    }

    private void unlock(String path) throws Exception {
        interProcessMutex.computeIfAbsent(path, key -> new InterProcessMutex(client, path));
        interProcessMutex.get(path).release();
    }
}
