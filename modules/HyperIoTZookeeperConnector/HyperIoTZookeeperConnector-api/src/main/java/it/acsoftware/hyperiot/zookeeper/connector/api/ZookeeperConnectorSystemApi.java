package it.acsoftware.hyperiot.zookeeper.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import org.apache.curator.CuratorZookeeperClient;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.recipes.leader.LeaderLatchListener;
import org.apache.zookeeper.CreateMode;

/**
 * @author Aristide Cittadino Interface component for %- projectSuffixUC SystemApi. This
 * interface defines methods for additional operations.
 */
public interface ZookeeperConnectorSystemApi extends HyperIoTBaseSystemApi {

    /**
     * This method adds a LeaderLatchListener
     *
     * @param listener       LeaderLatchListener instance
     * @param leadershipPath ZkNode path which bind listener on
     */
    void addListener(LeaderLatchListener listener, String leadershipPath);

    /**
     * This method returns true if current node is leader on given zkNode path, false otherwise
     *
     * @param mutexPath ZkNode path
     * @return True if current node is leader on given zkNode path, false otherwise
     */
    boolean isLeader(String mutexPath);

    /**
     * @param mode
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    void create(CreateMode mode, String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    void create(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     * @param path
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    void createEphemeral(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     *
     * @param path
     * @throws Exception
     */
    void delete(String path) throws Exception;

    /**
     *
     */
    /**
     * @param path
     * @return
     * @throws Exception
     */
    byte[] read(String path) throws Exception;

    /**
     *
     * @param path
     * @return
     * @throws Exception
     */
    boolean checkExists(String path) throws Exception;

    /**
     *
     * @return
     */
    CuratorFramework getZookeeperCuratorClient();
}
