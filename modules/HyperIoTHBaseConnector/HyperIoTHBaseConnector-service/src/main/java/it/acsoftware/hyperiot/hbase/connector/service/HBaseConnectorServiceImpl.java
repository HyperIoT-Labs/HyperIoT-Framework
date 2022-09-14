package it.acsoftware.hyperiot.hbase.connector.service;

import com.google.protobuf.ServiceException;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.security.annotations.AllowGenericPermissions;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseServiceImpl;
import it.acsoftware.hyperiot.hbase.connector.actions.HBaseConnectorAction;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorApi;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorSystemApi;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.List;
import java.util.Map;



/**
 * @author Aristide Cittadino Implementation class of HBaseConnectorApi interface.
 * It is used to implement all additional methods in order to interact with the system layer.
 */
@Component(service = HBaseConnectorApi.class, immediate = true)
public final class HBaseConnectorServiceImpl extends HyperIoTBaseServiceImpl implements HBaseConnectorApi {
    public static final String HBASE_CONNECTOR_RESOURCE_NAME = "it.acsoftware.hyperiot.hbase.connector.model.HBaseConnector";
    /**
     * Injecting the HBaseConnectorSystemApi
     */
    private HBaseConnectorSystemApi systemService;

    /**
     * @return The current HBaseConnectorSystemApi
     */
    protected HBaseConnectorSystemApi getSystemService() {
        getLog().debug( "invoking getSystemService, returning: " + this.systemService);
        return systemService;
    }

    /**
     * @param hBaseConnectorSystemService Injecting via OSGi DS current systemService
     */
    @Reference
    protected void setSystemService(HBaseConnectorSystemApi hBaseConnectorSystemService) {
        getLog().debug( "invoking setSystemService, setting: " + systemService);
        this.systemService = hBaseConnectorSystemService;
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.CHECK_CONNECTION, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void checkConnection(HyperIoTContext context) throws IOException, HyperIoTUnauthorizedException, ServiceException {
        systemService.checkConnection();
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.CREATE_TABLE, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void createTable(HyperIoTContext context, String tableName, List<String> columnFamilies) throws IOException, HyperIoTUnauthorizedException {
        systemService.createTable(tableName, columnFamilies);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.DELETE_DATA, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void deleteData(HyperIoTContext context, String tableName, String rowKey)
        throws IOException, HyperIoTUnauthorizedException {
        systemService.deleteData(tableName, rowKey);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.DISABLE_TABLE, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void disableTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException {
        systemService.disableTable(tableName);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.DROP_TABLE, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void dropTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException {
        systemService.dropTable(tableName);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.ENABLE_TABLE, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void enableTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException {
        systemService.enableTable(tableName);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.INSERT_DATA, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public void insertData(HyperIoTContext context, String tableName, String rowKey, String columnFamily, String column, String cellValue)
        throws IOException, HyperIoTUnauthorizedException {
        systemService.insertData(tableName, rowKey, columnFamily, column, cellValue);
    }

    @Override
    @AllowGenericPermissions(actions = HBaseConnectorAction.Names.READ_DATA, resourceName = HBASE_CONNECTOR_RESOURCE_NAME)
    public Map<byte[], Map<byte[], Map<byte[], byte[]>>> scan(HyperIoTContext context, String tableName, Map<byte[], List<byte[]>> columns,
                                                              byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit)
        throws IOException {
        return systemService.scan(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit);
    }

}
