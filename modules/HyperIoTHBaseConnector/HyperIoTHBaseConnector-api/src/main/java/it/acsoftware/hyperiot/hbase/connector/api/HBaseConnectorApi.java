package it.acsoftware.hyperiot.hbase.connector.api;

import com.google.protobuf.ServiceException;
import it.acsoftware.hyperiot.base.api.HyperIoTBaseApi;
import it.acsoftware.hyperiot.base.api.HyperIoTContext;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * 
 * @author Aristide Cittadino Interface component for HBaseConnectorApi. This interface
 *         defines methods for additional operations.
 *
 */
public interface HBaseConnectorApi extends HyperIoTBaseApi {

    /**
     * It checks if there's an active connection
     * @param context HyperIoTContext
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     * @throws ServiceException ServiceException
     */
    void checkConnection(HyperIoTContext context) throws IOException, HyperIoTUnauthorizedException, ServiceException;

    /**
     * It creates HBase Table
     * @param context HyperIoTContext
     * @param tableName Table name
     * @param columnFamilies HBase table column families
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void createTable(HyperIoTContext context, String tableName, List<String> columnFamilies) throws IOException, HyperIoTUnauthorizedException;

    /**
     * It deletes data
     * @param context HyperIoTContext
     * @param tableName Table name
     * @param rowKey Row key
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void deleteData(HyperIoTContext context, String tableName, String rowKey)
            throws IOException, HyperIoTUnauthorizedException;

    /**
     * It disables HBase table
     * @param context HyperIoTContext
     * @param tableName Table name
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void disableTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException;

    /**
     * It drops HBase table
     * @param context HyperIoTContext
     * @param tableName Table name
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void dropTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException;

    /**
     * It enables HBase table
     * @param context HyperIoTContext
     * @param tableName Table name
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void enableTable(HyperIoTContext context, String tableName) throws IOException, HyperIoTUnauthorizedException;

    /**
     * It inserts data
     * @param context HyperIoTContext
     * @param tableName Table name
     * @param rowKey Row key
     * @param columnFamily Column Family
     * @param column Column
     * @param cellValue Value to insert
     * @throws IOException IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void insertData(HyperIoTContext context, String tableName, String rowKey, String columnFamily, String column, String cellValue)
            throws IOException, HyperIoTUnauthorizedException;

    /**
     * It scans HBase table
     * @param context HyperIoTContext
     * @param tableName table name
     * @param columns map where keys are column families and values are columns belonging to each family
     * @param rowKeyLowerBound row key lower bound
     * @param rowKeyUpperBound row key upper bound
     * @param limit scan limit
     * @return map
     * @throws IOException IOException
     */
    @SuppressWarnings("unused")
    Map<byte[], Map<byte[], Map<byte[], byte[]>>> scan(HyperIoTContext context, String tableName, Map<byte[],
            List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit) throws IOException;

}