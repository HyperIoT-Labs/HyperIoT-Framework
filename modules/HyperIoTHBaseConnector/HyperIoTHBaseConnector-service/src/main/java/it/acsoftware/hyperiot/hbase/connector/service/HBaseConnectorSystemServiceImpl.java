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

package it.acsoftware.hyperiot.hbase.connector.service;

import com.google.protobuf.ServiceException;
import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.hbase.connector.actions.HBaseConnectorAction;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorSystemApi;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorUtil;
import it.acsoftware.hyperiot.hbase.connector.model.HBaseConnector;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.HBaseConfiguration;
import org.apache.hadoop.hbase.HColumnDescriptor;
import org.apache.hadoop.hbase.HTableDescriptor;
import org.apache.hadoop.hbase.TableName;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.client.coprocessor.AggregationClient;
import org.apache.hadoop.hbase.client.coprocessor.LongColumnInterpreter;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.util.Bytes;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;

import java.io.IOException;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author Aristide Cittadino Implementation class of the HBaseConnectorSystemApi
 * interface. This  class is used to implements all additional
 * methods to interact with the persistence layer.
 */
@Component(service = HBaseConnectorSystemApi.class, immediate = true)
public final class HBaseConnectorSystemServiceImpl extends HyperIoTBaseSystemServiceImpl implements HBaseConnectorSystemApi {

    private Configuration configuration;
    private HBaseConnectorUtil hBaseConnectorUtil;
    private Admin admin;
    private Connection connection;
    private int maxScanPageSize;       // max number of retrieved rows from a single scan
    private PermissionSystemApi permissionSystemApi;
    private ExecutorService hbaseThreadPool;

    @Activate
    public void activate() throws IOException {
        initHBaseThreadPool();
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    // set HBase configurations
                    setHBaseConfiguration();
                    // create a connection to the database
                    connection = ConnectionFactory.createConnection(configuration);
                    // get admin object, which manipulate database structure
                    admin = connection.getAdmin();
                } catch (IOException e) {
                    getLog().error(e.getMessage(), e);
                }
            }
        });
        t.start();
        checkRegisteredUserRoleExists();
    }

    /**
     * Shutdown executor service after stopping application
     */
    @Deactivate
    public void deactivate() throws InterruptedException {
        // stop taking new tasks
        this.hbaseThreadPool.shutdown();
        try {
            if (!this.hbaseThreadPool.awaitTermination(hBaseConnectorUtil.getAwaitTermination(), TimeUnit.MILLISECONDS)) {
                // wait up to 1000 ms for all tasks to be completed
                this.hbaseThreadPool.shutdownNow();
            }
        } catch (InterruptedException e) {
            // if time expires, stop execution immediately
            this.hbaseThreadPool.shutdownNow();
            throw e;
        }
    }

    private void initHBaseThreadPool() {
        this.hbaseThreadPool = new ThreadPoolExecutor(hBaseConnectorUtil.getCorePoolSize(), hBaseConnectorUtil.getMaximumPoolSize(),
                hBaseConnectorUtil.getKeepAliveTime(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void executeTask(Runnable runnableTask) {
        this.hbaseThreadPool.execute(runnableTask);
    }

    @Override
    public void checkConnection() throws IOException, ServiceException {
        HBaseAdmin.checkHBaseAvailable(configuration);
    }

    private void checkRegisteredUserRoleExists() {
        String resourceName = HBaseConnector.class.getName();
        List<HyperIoTAction> actions = new ArrayList<>();
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.CREATE_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.DISABLE_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.DROP_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.READ_DATA));
        this.permissionSystemApi
                .checkOrCreateRoleWithPermissions(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER, actions);
    }

    @Override
    public void createTable(String tableName, List<String> columnFamilies) throws IOException {
        TableName table = TableName.valueOf(tableName);
        HTableDescriptor descriptor = new HTableDescriptor(table);
        // add column families to table descriptor
        columnFamilies.forEach((columnFamily) -> descriptor.addFamily(new HColumnDescriptor(columnFamily)));
        admin.createTable(descriptor);
    }

    @Override
    public void createTableAsync(String tableName, List<String> columnFamilies) {
        Runnable runnableTask = () -> {
            try {
                this.createTable(tableName, columnFamilies);
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    @Override
    public void deleteData(String tableName, String rowKey)
            throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Delete delete = new Delete(Bytes.toBytes(rowKey));
        table.delete(delete);
        table.close();
    }

    @Override
    public void deleteDataAsync(String tableName, String rowKey) {
        Runnable runnableTask = () -> {
            try {
                this.deleteData(tableName, rowKey);
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    @Override
    public void disableTable(String tableName) throws IOException {
        admin.disableTable(TableName.valueOf(tableName));
    }

    @Override
    public void disableTableAsync(String tableName) {
        Runnable runnableTask = () -> {
            try {
                this.disableTable(tableName);
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    @Override
    public void disableAndDropTable(String tableName) throws IOException, HyperIoTUnauthorizedException {
        admin.disableTable(TableName.valueOf(tableName));
        admin.deleteTable(TableName.valueOf(tableName));
    }

    @Override
    public void disableAndDropTableAsync(String tableName) {
        Runnable runnableTask = () -> {
            try {
                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    @Override
    public void dropTable(String tableName) throws IOException {
        admin.deleteTable(TableName.valueOf(tableName));
    }

    @Override
    public void dropTableAsync(String tableName) {
        Runnable runnableTask = () -> {
            try {
                admin.deleteTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    @Override
    public void enableTable(String tableName) throws IOException {
        admin.enableTable(TableName.valueOf(tableName));
    }

    @Override
    public void insertData(String tableName, String rowKey, String columnFamily, String column, String cellValue)
            throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        Put put = new Put(rowKey.getBytes());
        put.addImmutable(columnFamily.getBytes(), column.getBytes(), cellValue.getBytes());
        table.put(put);
        table.close();
    }

    @Override
    public void insertDataAsync(String tableName, String rowKey, String columnFamily, String column, String cellValue) {
        Runnable runnableTask = () -> {
            try {
                insertData(tableName, rowKey, columnFamily, column, cellValue);
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        this.executeTask(runnableTask);
    }

    private List<Filter> getExtraFilters(byte[] column) {
        List<Filter> extraFilters = null;
        if (column != null) {
            Filter qualifierFilter = new QualifierFilter(CompareFilter.CompareOp.EQUAL, new BinaryComparator(column));
            extraFilters = Collections.singletonList(qualifierFilter);
        }
        return extraFilters;
    }

    @Override
    public ResultScanner getScanner(String tableName, Map<byte[], List<byte[]>> columns,
                                    byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit)
            throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        // get scan
        Scan scan = getScan(columns, rowKeyLowerBound, rowKeyUpperBound, limit, null);
        return table.getScanner(scan);
    }

    private Scan getScan(Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound,
                         byte[] rowKeyUpperBound, int limit, List<Filter> extraFilters) {
        Filter rowFilterLowerBound =
                new RowFilter(CompareFilter.CompareOp.GREATER_OR_EQUAL, new BinaryComparator(rowKeyLowerBound));
        Filter rowFilterUpperBound =
                new RowFilter(CompareFilter.CompareOp.LESS_OR_EQUAL, new BinaryComparator(rowKeyUpperBound));
        List<Filter> rowFilterList;
        if (limit < 0)
            rowFilterList = new ArrayList<>(Arrays.asList(rowFilterLowerBound, rowFilterUpperBound));
        else {
            // if limit is not equal to 0 and not greater than maxScanPageSize, set it
            PageFilter pageFilter = new PageFilter(limit > 0 && limit <= maxScanPageSize ? limit : maxScanPageSize);
            rowFilterList = new ArrayList<>(Arrays.asList(rowFilterLowerBound, rowFilterUpperBound, pageFilter));
        }
        Scan scan = new Scan();
        for (byte[] columnFamily : columns.keySet()) {
            if (columns.get(columnFamily) == null || columns.get(columnFamily).isEmpty())
                scan.addFamily(columnFamily);
            else
                for (byte[] column : columns.get(columnFamily))
                    scan.addColumn(columnFamily, column);
        }
        if (extraFilters != null) {
            // add extra filters
            rowFilterList.addAll(extraFilters);
        }
        scan.setFilter(new FilterList(FilterList.Operator.MUST_PASS_ALL, rowFilterList));
        return scan;
    }

    @Override
    public long rowCount(String tableName, byte[] columnFamily, byte[] column, byte[] rowKeyLowerBound,
                         byte[] rowKeyUpperBound) throws Throwable {
        try (AggregationClient aggregationClient = new AggregationClient(configuration)) {
            // specify column families and columns on which perform scan
            Map<byte[], List<byte[]>> columns = new HashMap<>();
            columns.put(columnFamily, new ArrayList<>());
            if (column != null)
                columns.get(columnFamily).add(column);
            // get scan
            List<Filter> extraFilters = getExtraFilters(column);
            Scan scan = getScan(columns, rowKeyLowerBound, rowKeyUpperBound, -1, extraFilters);
            return aggregationClient.rowCount(connection.getTable(TableName.valueOf(tableName)), new LongColumnInterpreter(), scan);
        }
    }

    @Override
    public long rowCount(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws Throwable {
        try (AggregationClient aggregationClient = new AggregationClient(configuration)) {
            Scan scan = getScan(columns, rowKeyLowerBound, rowKeyUpperBound, -1, null);
            return aggregationClient.rowCount(connection.getTable(TableName.valueOf(tableName)), new LongColumnInterpreter(), scan);
        }
    }

    @Override
    public List<byte[]> scan(String tableName, byte[] columnFamily, byte[] column,
                             byte[] rowKeyLowerBound, byte[] rowKeyUpperBound)
            throws IOException {
        // specify column families and columns on which perform scan
        Map<byte[], List<byte[]>> columns = new HashMap<>();
        columns.put(columnFamily, new ArrayList<>());
        columns.get(columnFamily).add(column);
        // get scan
        ResultScanner scanner =
                getScanner(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, 0);
        // collect scan result
        List<byte[]> cells = new ArrayList<>();
        for (Result result : scanner)
            cells.add(result.getValue(columnFamily, column));
        return cells;
    }

    @Override
    public List<Result> scanWithCompleteResult(String tableName, Map<byte[], List<byte[]>> columns,
                                               byte[] rowKeyLowerBound, byte[] rowKeyUpperBound,int limit)
            throws IOException {
        // get scan
        ResultScanner scanner =
                getScanner(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit);
        // collect scan result
        List<Result> results = new ArrayList<>();
        for (Result result : scanner)
            results.add(result);
        return results;
    }

    @Override
    public Map<byte[], Map<byte[], Map<byte[], byte[]>>> scan(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound,
                                                              byte[] rowKeyUpperBound, int limit) throws IOException {
        Map<byte[], Map<byte[], Map<byte[], byte[]>>> output = new HashMap<>();
        ResultScanner scanner = getScanner(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit);
        for (Result result : scanner) {
            output.put(result.getRow(), new HashMap<>());
            for (byte[] columnFamily : columns.keySet()) {
                output.get(result.getRow()).put(columnFamily, new HashMap<>());
                for (byte[] column : columns.get(columnFamily)) {
                    byte[] value = result.getValue(columnFamily, column);
                    if (value != null)
                        output.get(result.getRow()).get(columnFamily).put(column, result.getValue(columnFamily, column));
                }
            }
        }
        return output;
    }

    /**
     * Initialization method: HBase client configuration
     */
    private void setHBaseConfiguration() {
        configuration = HBaseConfiguration.create();
        configuration.setBoolean("hbase.cluster.distributed", hBaseConnectorUtil.getClusterDistributed());
        configuration.set("hbase.master", hBaseConnectorUtil.getMaster());
        configuration.set("hbase.master.hostname", hBaseConnectorUtil.getMasterHostname());
        configuration.setInt("hbase.master.info.port", hBaseConnectorUtil.getMasterInfoPort());
        configuration.setInt("hbase.master.port", hBaseConnectorUtil.getMasterPort());
        configuration.setInt("hbase.regionserver.info.port", hBaseConnectorUtil.getRegionserverInfoPort());
        configuration.setInt("hbase.regionserver.port", hBaseConnectorUtil.getRegionserverPort());
        configuration.set("hbase.rootdir", hBaseConnectorUtil.getRootdir());
        configuration.set("hbase.zookeeper.quorum", hBaseConnectorUtil.getZookeeperQuorum());
        configuration.set("hbase.coprocessor.user.region.classes", "org.apache.hadoop.hbase.coprocessor.AggreagateImplementation");
        maxScanPageSize = hBaseConnectorUtil.getMaxScanPageSize();
    }

    @Override
    public boolean tableExists(String tableName) throws IOException {
        return admin.tableExists(TableName.valueOf(tableName));
    }

    @Reference
    public void setHBaseConnectorUtil(HBaseConnectorUtil hBaseConnectorUtil) {
        this.hBaseConnectorUtil = hBaseConnectorUtil;
    }

    @Reference
    public void setPermissionSystemApi(PermissionSystemApi permissionSystemApi) {
        this.permissionSystemApi = permissionSystemApi;
    }

}
