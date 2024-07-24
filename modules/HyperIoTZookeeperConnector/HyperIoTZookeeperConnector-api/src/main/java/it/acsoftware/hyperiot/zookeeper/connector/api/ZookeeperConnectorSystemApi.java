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
     * @param data
     * @param createParentFolders
     * @throws Exception
     */
    void createPersistent(String path, byte[] data, boolean createParentFolders) throws Exception;

    /**
     *
     * @param path
     * @throws Exception
     */
    void delete(String path) throws Exception;

    /**
     * @param path
     * @return
     * @throws Exception
     */
    byte[] read(String path) throws Exception;

    /**
     *
     * @param path
     * @param lock if path should be locked while reading
     * @return
     * @throws Exception
     */
    byte[] read(String path,boolean lock) throws Exception;

    /**
     *
     * @param path path to update
     * @param data data to update
     * @throws Exception
     */
    void update(String path, byte[] data) throws Exception;
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
