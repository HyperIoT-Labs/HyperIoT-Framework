package it.acsoftware.hyperiot.zookeeper.connector.service;

import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.zookeeper.connector.actions.ZookeeperConnectorAction;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorApi;
import it.acsoftware.hyperiot.zookeeper.connector.api.ZookeeperConnectorSystemApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;



/**
 * @author Aristide Cittadino Implementation class of ZookeeperConnectorApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = ZookeeperConnectorApi.class, immediate = true)
public final class ZookeeperConnectorServiceImpl extends HyperIoTBaseServiceImpl implements ZookeeperConnectorApi {
    public static final String ZOOKEEPER_CONNECTOR_RESOURCE_NAME = "it.acsoftware.hyperiot.zookeeper.connector.model.ZookeeperConnector";
    /**
     * Injecting the ZookeeperConnectorSystemApi
     */
    private ZookeeperConnectorSystemApi systemService;

    /**
     * @return The current ZookeeperConnectorSystemApi
     */
    protected ZookeeperConnectorSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: {}", this.systemService);
        return systemService;
    }

    /**
     * @param zookeeperConnectorSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(ZookeeperConnectorSystemApi zookeeperConnectorSystemService) {
        getLog().debug( "invoking setSystemService, setting: {}", systemService);
        this.systemService = zookeeperConnectorSystemService;
    }

    @AllowGenericPermissions(actions = ZookeeperConnectorAction.Names.CHECK_LEADERSHIP, resourceName = ZOOKEEPER_CONNECTOR_RESOURCE_NAME)
    public boolean isLeader(HyperIoTContext hyperIoTContext, String mutexPath) {
        return systemService.isLeader(mutexPath);
    }

}
