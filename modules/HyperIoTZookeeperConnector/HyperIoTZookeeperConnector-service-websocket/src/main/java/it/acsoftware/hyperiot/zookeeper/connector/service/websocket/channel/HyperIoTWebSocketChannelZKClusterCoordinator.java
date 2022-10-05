package it.acsoftware.hyperiot.zookeeper.connector.service.websocket.channel;

import it.acsoftware.hyperiot.base.model.HyperIoTClusterNodeInfo;
import it.acsoftware.hyperiot.base.util.HyperIoTUtil;
import it.acsoftware.hyperiot.websocket.api.channel.*;
import it.acsoftware.hyperiot.websocket.channel.factory.HyperIoTWebSocketChannelFactory;
import it.acsoftware.hyperiot.websocket.channel.role.HyperIoTWebSocketChannelRoleManager;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketConstants;
import it.acsoftware.hyperiot.websocket.model.HyperIoTWebSocketUserInfo;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorSystemApi;
import it.acsoftware.hyperiot.zookeeper.connector.model.HyperIoTZooKeeperData;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.cache.ChildData;
import org.apache.curator.framework.recipes.cache.CuratorCache;
import org.apache.curator.framework.recipes.cache.CuratorCacheBuilder;
import org.apache.curator.framework.recipes.cache.CuratorCacheListener;
import org.apache.zookeeper.CreateMode;
import org.osgi.service.component.annotations.Component;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @Author Aristide Cittadino
 */
@Component(service = HyperIoTWebSocketChannelClusterCoordinator.class, property = {
        HyperIoTWebSocketConstants.CHANNEL_CLUSTER_COORDINATOR_OSGI_FILTER_NAME + "=zookeeper-cluster-coordinator"
})
public class HyperIoTWebSocketChannelZKClusterCoordinator implements HyperIoTWebSocketChannelClusterCoordinator {
    private static Logger logger = LoggerFactory.getLogger(HyperIoTWebSocketChannelZKClusterCoordinator.class);
    private static final String GLOBAL_CHANNELS_PATH = "/" + HyperIoTUtil.getLayer() + "/websockets/channels";
    private static final String CHANNELS_PARTECIPANTS_PATH = GLOBAL_CHANNELS_PATH + "/{channelId}/partecipants/";
    private static final String CHANNEL_EVENT_PATH_PATTERN = GLOBAL_CHANNELS_PATH.replaceAll("/", "\\/") + "\\/([a-zA-Z0-9\\-_]*)";
    private static final String CHANNEL_PARTECIPANT_EVENT_PATH_PATTENR = CHANNEL_EVENT_PATH_PATTERN + "\\/partecipants\\/([a-zA-Z0-9\\-_]*)";

    private static final Pattern channelPattern = Pattern.compile(CHANNEL_EVENT_PATH_PATTERN);
    private static final Pattern partecipantPattern = Pattern.compile(CHANNEL_PARTECIPANT_EVENT_PATH_PATTENR);

    private static final String CHANNEL_EVENT_CLUSTER_SOURCE_NODE_DATA_FIELD = "sourceNode";
    private static final String CHANNEL_ZK_DATA_FIELD = "channel";
    private static final String CHANNEL_CLASS_ZK_DATA_FIELD = "channelClass";

    private static final String PARTECIPANT_ZK_DATA_FIELD = "partecipant";
    private static final String PARTECIPANT_ROLES_ZK_DATA_FIELD = "roles";

    private ZookeeperConnectorSystemApi zookeeperConnectorSystemApi;
    private CuratorCache curatorCache;
    private CuratorCacheListener listener;

    private HyperIoTWebSocketChannelManager channelManager;

    public HyperIoTWebSocketChannelZKClusterCoordinator(){
        this.zookeeperConnectorSystemApi = (ZookeeperConnectorSystemApi) HyperIoTUtil.getService(ZookeeperConnectorSystemApi.class);
        init(zookeeperConnectorSystemApi);
    }

    public HyperIoTWebSocketChannelZKClusterCoordinator(ZookeeperConnectorSystemApi zookeeperConnectorSystemApi) {
        init(zookeeperConnectorSystemApi);
    }

    private void init(ZookeeperConnectorSystemApi zookeeperConnectorSystemApi){
        this.zookeeperConnectorSystemApi = zookeeperConnectorSystemApi;
        this.registerToZookeper();
    }

    private void registerToZookeper() {
        checkNodeExistOrCreateIt();
        CuratorCacheBuilder curatorCacheBuilder = CuratorCache.builder(this.zookeeperConnectorSystemApi.getZookeeperCuratorClient(), GLOBAL_CHANNELS_PATH);
        this.curatorCache = curatorCacheBuilder.build();
        this.listener = CuratorCacheListener.builder().forAll((type, oldData, newData) -> {
            zkEvent(type, oldData, newData);
        }).build();
        this.curatorCache.listenable().addListener(listener);
        this.curatorCache.start();
    }

    private void checkNodeExistOrCreateIt() {
        try {
            if (!this.zookeeperConnectorSystemApi.checkExists(GLOBAL_CHANNELS_PATH)) {
                this.zookeeperConnectorSystemApi.create(CreateMode.PERSISTENT, GLOBAL_CHANNELS_PATH, null, true);
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public Map<String, HyperIoTWebSocketChannel> connectNewPeer(HyperIoTWebSocketChannelManager hyperIoTWebSocketChannelManager) {
        CuratorFramework curatorFramework = this.zookeeperConnectorSystemApi.getZookeeperCuratorClient();
        this.channelManager = hyperIoTWebSocketChannelManager;
        Map<String, HyperIoTWebSocketChannel> channels = new HashMap<>();
        try {
            curatorFramework.getChildren().forPath(GLOBAL_CHANNELS_PATH)
                    .stream()
                    .parallel().forEach(path -> {
                        HyperIoTWebSocketChannel channel = loadChannel(path);
                        if (channel != null) {
                            channels.put(channel.getChannelId(), channel);
                            if(!path.startsWith("/")){
                                path = "/"+path;
                            }
                            String partecipantsPath = path + CHANNELS_PARTECIPANTS_PATH.replace("${channelId}", channel.getChannelId());
                            Map<HyperIoTWebSocketUserInfo, Set<HyperIoTWebSocketChannelRole>> partecipants = loadChannelsPartecipants(partecipantsPath);
                            partecipants.keySet().parallelStream().forEach(partecipant -> {
                                channel.addPartecipantInfo(partecipant, partecipants.get(partecipant));
                            });
                        }
                    });
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return channels;
    }

    @Override
    public HyperIoTWebSocketChannelManager getRegisteredWebSocketChannelManager() {
        return this.channelManager;
    }

    @Override
    public void disconnectPeer() {
        //Disconnects from zookeeper
        this.curatorCache.listenable().removeListener(listener);
        this.curatorCache.close();
    }

    @Override
    public void notifyChannelAdded(HyperIoTClusterNodeInfo sourceNode,HyperIoTWebSocketChannel hyperIoTWebSocketChannel) {
        try {
            HyperIoTZooKeeperData data = new HyperIoTZooKeeperData();
            String path = GLOBAL_CHANNELS_PATH + "/" + hyperIoTWebSocketChannel.getChannelId();
            data.addParam(CHANNEL_ZK_DATA_FIELD, hyperIoTWebSocketChannel.toJson().getBytes(StandardCharsets.UTF_8));
            data.addParam(CHANNEL_CLASS_ZK_DATA_FIELD, hyperIoTWebSocketChannel.getClass().getName().getBytes(StandardCharsets.UTF_8));
            data.addParam(CHANNEL_EVENT_CLUSTER_SOURCE_NODE_DATA_FIELD,sourceNode.toJson().getBytes(StandardCharsets.UTF_8));
            logger.debug("Registering Channel info on zookeeper  \n {}", new Object[]{hyperIoTWebSocketChannel.toJson()});
            zookeeperConnectorSystemApi.create(path, data.getBytes(), true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void notifyChannelDeleted(String channelId) {
        String path = GLOBAL_CHANNELS_PATH + "/" + channelId;
        deleteZKPath(path);
    }

    @Override
    public void notifyPartecipantAdded(String channelId, HyperIoTWebSocketUserInfo hyperIoTWebSocketUserInfo, Set<HyperIoTWebSocketChannelRole> roles) {
        try {
            HyperIoTZooKeeperData data = new HyperIoTZooKeeperData();
            String rolesCommaSeparatedList = HyperIoTWebSocketChannelRoleManager.rolesAsCommaSeparatedList(roles);
            String path = GLOBAL_CHANNELS_PATH + "/" + channelId + "/partecipants/" + hyperIoTWebSocketUserInfo.getUsername();
            data.addParam(PARTECIPANT_ZK_DATA_FIELD, hyperIoTWebSocketUserInfo.toJson().getBytes(StandardCharsets.UTF_8));
            data.addParam(PARTECIPANT_ROLES_ZK_DATA_FIELD, rolesCommaSeparatedList.getBytes(StandardCharsets.UTF_8));
            logger.debug("Registering Partecipant info on channel {} with info  \n {}", new Object[]{channelId, hyperIoTWebSocketUserInfo.toJson()});
            zookeeperConnectorSystemApi.create(path, data.getBytes(), true);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    @Override
    public void notifyPartecipantGone(String channelId, HyperIoTWebSocketUserInfo hyperIoTWebSocketUserInfo) {
        String path = GLOBAL_CHANNELS_PATH + "/" + channelId + "/partecipants/" + hyperIoTWebSocketUserInfo.getUsername();
        deleteZKPath(path);
    }

    @Override
    public void notifyPartecipantDisconnected(String channelId, HyperIoTWebSocketUserInfo hyperIoTWebSocketUserInfo) {
        String path = GLOBAL_CHANNELS_PATH + "/" + channelId + "/partecipants/" + hyperIoTWebSocketUserInfo.getUsername();
        deleteZKPath(path);
    }

    private void deleteZKPath(String path) {
        try {
            zookeeperConnectorSystemApi.delete(path);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
    }

    private HyperIoTWebSocketChannel loadChannel(String channelId) {
        try {
            String path = GLOBAL_CHANNELS_PATH + "/" + channelId;
            byte[] data = this.zookeeperConnectorSystemApi.read(path);
            HyperIoTZooKeeperData zkData = HyperIoTZooKeeperData.fromBytes(data);
            return loadChannel(zkData);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return null;
    }

    private HyperIoTWebSocketChannel loadChannel(HyperIoTZooKeeperData zkData) {
        if (zkData != null) {
            String channelClassStr = new String(zkData.getParam(CHANNEL_CLASS_ZK_DATA_FIELD));
            String channelData = new String(zkData.getParam(CHANNEL_ZK_DATA_FIELD));
            HyperIoTWebSocketChannel channel = HyperIoTWebSocketChannelFactory.createFromString(channelData, channelClassStr);
            return channel;
        }
        return null;
    }

    private Map<HyperIoTWebSocketUserInfo, Set<HyperIoTWebSocketChannelRole>> loadChannelsPartecipants(String path) {
        Map<HyperIoTWebSocketUserInfo, Set<HyperIoTWebSocketChannelRole>> partecipants = Collections.synchronizedMap(new HashMap<>());
        try {
            byte[] data = this.zookeeperConnectorSystemApi.read(path);
            HyperIoTZooKeeperData zkData = HyperIoTZooKeeperData.fromBytes(data);
            HyperIoTWebSocketUserInfo info = HyperIoTWebSocketUserInfo.fromString(new String(zkData.getParam(PARTECIPANT_ZK_DATA_FIELD)));
            String rolesCommaSeparatedList = new String(zkData.getParam(PARTECIPANT_ROLES_ZK_DATA_FIELD));
            Set<HyperIoTWebSocketChannelRole> roles = HyperIoTWebSocketChannelRoleManager.fromCommaSeparatedList(rolesCommaSeparatedList);
            partecipants.put(info, roles);
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
        }
        return partecipants;
    }

    public void zkEvent(CuratorCacheListener.Type type, ChildData oldData, ChildData data) {
        String eventPath = (data != null) ? data.getPath() : (oldData != null) ? oldData.getPath() : null;
        logger.debug("New ZK Event on path {}, with type {}", new Object[]{eventPath, type.toString()});
        // Now create matcher object.
        Matcher m = partecipantPattern.matcher(eventPath);
        if (m.matches()) {
            partecipantEvent(type, m.group(1), oldData, data);
        } else {
            m = channelPattern.matcher(eventPath);
            if (m.matches()) {
                //channel event
                channelEvent(type, m.group(1), oldData, data);
            }
        }
    }

    private void partecipantEvent(CuratorCacheListener.Type type, String channelId,ChildData oldData, ChildData data) {
        if (type.equals(CuratorCacheListener.Type.NODE_CREATED)) {
            HyperIoTZooKeeperData zkData = HyperIoTZooKeeperData.fromBytes(data.getData());
            HyperIoTWebSocketUserInfo info = HyperIoTWebSocketUserInfo.fromString(new String(zkData.getParam(PARTECIPANT_ZK_DATA_FIELD)));
            String rolesCommaSeparatedList = new String(zkData.getParam(PARTECIPANT_ROLES_ZK_DATA_FIELD));
            Set<HyperIoTWebSocketChannelRole> roles = HyperIoTWebSocketChannelRoleManager.fromCommaSeparatedList(rolesCommaSeparatedList);
            //partecipant added
            //processing event if the current node is not the one who generated it
            //reduntand but makes code more readable
            if(!info.getClusterNodeInfo().isOnLocalNode())
                this.channelManager.onPartecipantAdded(channelId, info, roles);
        } else if (type.equals(CuratorCacheListener.Type.NODE_CHANGED)) {
            //partecipant changed
            //do nothing
        } else if (type.equals(CuratorCacheListener.Type.NODE_DELETED)) {
            //partecipant gone
            HyperIoTZooKeeperData zkData = HyperIoTZooKeeperData.fromBytes(oldData.getData());
            HyperIoTWebSocketUserInfo info = HyperIoTWebSocketUserInfo.fromString(new String(zkData.getParam(PARTECIPANT_ZK_DATA_FIELD)));
            if(!info.getClusterNodeInfo().isOnLocalNode())
                this.channelManager.onPartecipantGone(channelId, info);
        }
    }

    private void channelEvent(CuratorCacheListener.Type type, String channelId,ChildData oldData, ChildData data) {
        if (type.equals(CuratorCacheListener.Type.NODE_CREATED)) {
            HyperIoTZooKeeperData zkData = HyperIoTZooKeeperData.fromBytes(data.getData());
            HyperIoTClusterNodeInfo clusterNode = HyperIoTClusterNodeInfo.fromString(new String(zkData.getParam(CHANNEL_EVENT_CLUSTER_SOURCE_NODE_DATA_FIELD)));
            //skip the event if this node is the one that has generated it
            if(!clusterNode.isOnLocalNode()) {
                HyperIoTWebSocketChannel channel = loadChannel(zkData);
                if (channel != null)
                    this.channelManager.onChannelAdded(channel);
            }
        } else if (type.equals(CuratorCacheListener.Type.NODE_CHANGED)) {
            //channel updated
        } else if (type.equals(CuratorCacheListener.Type.NODE_DELETED)) {
            //channel deleted
            //no worries since if the channel has been delete it won't be found
            this.channelManager.onChannelRemoved(channelId);
        }
    }

}
