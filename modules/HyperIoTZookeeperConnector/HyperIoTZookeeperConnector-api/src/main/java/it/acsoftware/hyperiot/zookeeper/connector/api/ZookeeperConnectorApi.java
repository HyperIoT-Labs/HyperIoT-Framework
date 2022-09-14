package it.acsoftware.hyperiot.zookeeper.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;

/**
 * 
 * @author Aristide Cittadino Interface component for ZookeeperConnectorApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface ZookeeperConnectorApi extends HyperIoTBaseApi {

    boolean isLeader(HyperIoTContext hyperIoTContext, String mutexPath);

}