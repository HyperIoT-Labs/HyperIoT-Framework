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

import it.acsoftware.hyperiot.base.action.util.HyperIoTActionsUtil;
import it.acsoftware.hyperiot.base.api.HyperIoTAction;
import it.acsoftware.hyperiot.base.service.HyperIoTBaseSystemServiceImpl;
import it.acsoftware.hyperiot.hbase.connector.actions.HBaseConnectorAction;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorSystemApi;
import it.acsoftware.hyperiot.hbase.connector.api.HBaseConnectorUtil;
import it.acsoftware.hyperiot.hbase.connector.model.HBaseConnector;
import it.acsoftware.hyperiot.permission.api.PermissionSystemApi;
import it.acsoftware.hyperiot.role.util.HyperIoTRoleConstants;
import org.apache.hadoop.conf.Configuration;
import org.apache.hadoop.hbase.*;
import org.apache.hadoop.hbase.client.*;
import org.apache.hadoop.hbase.filter.*;
import org.apache.hadoop.hbase.security.provider.SimpleSaslAuthenticationProvider;
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
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

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
    public void activate() {
        initHBaseThreadPool();
        Thread t = new Thread(() -> {
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
        this.hbaseThreadPool = new ThreadPoolExecutor(hBaseConnectorUtil.getCorePoolSize(), hBaseConnectorUtil.getMaximumPoolSize(), hBaseConnectorUtil.getKeepAliveTime(), TimeUnit.MILLISECONDS, new LinkedBlockingQueue<>());
    }

    public void executeTask(Runnable runnableTask) {
        this.hbaseThreadPool.execute(runnableTask);
    }

    @Override
    public void checkConnection() throws IOException {
        HBaseAdmin.available(configuration);
    }

    private void checkRegisteredUserRoleExists() {
        String resourceName = HBaseConnector.class.getName();
        List<HyperIoTAction> actions = new ArrayList<>();
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.CREATE_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.DISABLE_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.DROP_TABLE));
        actions.add(HyperIoTActionsUtil.getHyperIoTAction(resourceName, HBaseConnectorAction.READ_DATA));
        this.permissionSystemApi.checkOrCreateRoleWithPermissions(HyperIoTRoleConstants.ROLE_NAME_REGISTERED_USER, actions);
    }

    @Override
    public void createTable(String tableName, List<String> columnFamilies) {
        Runnable r = () -> {
            TableName table = TableName.valueOf(tableName);
            HTableDescriptor descriptor = new HTableDescriptor(table);
            // add column families to table descriptor
            columnFamilies.forEach((columnFamily) -> descriptor.addFamily(new HColumnDescriptor(columnFamily)));
            try {
                admin.createTable(descriptor);
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        //executing with karaf class loader switch
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void createTableAsync(String tableName, List<String> columnFamilies) {
        this.executeTask(() -> this.createTable(tableName, columnFamilies));
    }

    @Override
    public void deleteData(String tableName, String rowKey) {
        Runnable r = () -> {
            try {
                Table table = connection.getTable(TableName.valueOf(tableName));
                Delete delete = new Delete(Bytes.toBytes(rowKey));
                table.delete(delete);
                table.close();
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        //executing with karaf class loader switch
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void deleteDataAsync(String tableName, String rowKey) {
        this.executeTask(() -> this.deleteData(tableName, rowKey));
    }

    @Override
    public void disableTable(String tableName) {
        Runnable r = () -> {
            try {
                admin.disableTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        //executing with karaf class loader switch
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void disableTableAsync(String tableName) {
        this.executeTask(() -> disableTable(tableName));
    }

    @Override
    public void disableAndDropTable(String tableName) {
        Runnable r = () -> {
            try {
                admin.disableTable(TableName.valueOf(tableName));
                admin.deleteTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void disableAndDropTableAsync(String tableName) {
        this.executeTask(() -> disableAndDropTable(tableName));
    }

    @Override
    public void dropTable(String tableName) {
        Runnable r = () -> {
            try {
                admin.deleteTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void dropTableAsync(String tableName) {
        this.executeTask(() -> dropTable(tableName));
    }

    @Override
    public void enableTable(String tableName) {
        Runnable r = () -> {
            try {
                admin.enableTable(TableName.valueOf(tableName));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void insertData(String tableName, String rowKey, String columnFamily, String column, String cellValue) {
        Runnable r = () -> {
            try {
                Table table = connection.getTable(TableName.valueOf(tableName));
                Put put = new Put(rowKey.getBytes());
                put.addImmutable(columnFamily.getBytes(), column.getBytes(), cellValue.getBytes());
                table.put(put);
                table.close();
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        wrapInsideHBaseConnectorClassLoader(r);
    }

    @Override
    public void insertDataAsync(String tableName, String rowKey, String columnFamily, String column, String cellValue) {
        this.executeTask(() -> insertData(tableName, rowKey, columnFamily, column, cellValue));
    }

    @Override
    @Deprecated
    public ResultScanner getScanner(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit) throws IOException {
        Table table = connection.getTable(TableName.valueOf(tableName));
        // get scan
        Scan scan = getScan(columns, rowKeyLowerBound, rowKeyUpperBound, limit, null);
        return table.getScanner(scan);
    }

    private Scan getScan(Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit, List<Filter> extraFilters) {
        Filter rowFilterLowerBound = new RowFilter(CompareOperator.GREATER_OR_EQUAL, new BinaryComparator(rowKeyLowerBound));
        Filter rowFilterUpperBound = new RowFilter(CompareOperator.LESS_OR_EQUAL, new BinaryComparator(rowKeyUpperBound));
        List<Filter> rowFilterList;
        if (limit < 0) rowFilterList = new ArrayList<>(Arrays.asList(rowFilterLowerBound, rowFilterUpperBound));
        else {
            // if limit is not equal to 0 and not greater than maxScanPageSize, set it
            PageFilter pageFilter = new PageFilter(limit > 0 && limit <= maxScanPageSize ? limit : maxScanPageSize);
            rowFilterList = new ArrayList<>(Arrays.asList(rowFilterLowerBound, rowFilterUpperBound, pageFilter));
        }
        Scan scan = new Scan();
        for (byte[] columnFamily : columns.keySet()) {
            if (columns.get(columnFamily) == null || columns.get(columnFamily).isEmpty()) scan.addFamily(columnFamily);
            else for (byte[] column : columns.get(columnFamily))
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
    public long rowCount(String tableName, byte[] columnFamily, byte[] column, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws Throwable {
        Map<byte[], List<byte[]>> columns = new HashMap<>();
        columns.put(columnFamily, new ArrayList<>());
        if (column != null) columns.get(columnFamily).add(column);
        return rowCount(tableName, columns, rowKeyLowerBound, rowKeyUpperBound);
    }

    @Override
    public long rowCount(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws Throwable {
        AtomicLong counter = new AtomicLong();
        scanResults(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, -1, result -> {
            counter.set(counter.addAndGet(result.listCells().size()));
        });
        return counter.longValue();
    }

    @Override
    public List<byte[]> scan(String tableName, byte[] columnFamily, byte[] column, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws IOException {
        // specify column families and columns on which perform scan
        Map<byte[], List<byte[]>> columns = new HashMap<>();
        columns.put(columnFamily, new ArrayList<>());
        columns.get(columnFamily).add(column);
        // collect scan result
        List<byte[]> cells = new ArrayList<>();
        scanResults(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, 0, result -> {
            cells.add(result.getValue(columnFamily, column));
        });
        return cells;
    }

    @Override
    public List<Result> scanWithCompleteResult(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit) throws IOException {
        final List<Result> results = new ArrayList<>();
        scanResults(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit, result -> {
            results.add(result);
        });
        return results;
    }

    @Override
    public Map<byte[], Map<byte[], Map<byte[], byte[]>>> scan(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit) throws IOException {
        final Map<byte[], Map<byte[], Map<byte[], byte[]>>> output = new HashMap<>();
        scanResults(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit, result -> {
            output.put(result.getRow(), new HashMap<>());
            for (byte[] columnFamily : columns.keySet()) {
                output.get(result.getRow()).put(columnFamily, new HashMap<>());
                for (byte[] column : columns.get(columnFamily)) {
                    byte[] value = result.getValue(columnFamily, column);
                    if (value != null)
                        output.get(result.getRow()).get(columnFamily).put(column, result.getValue(columnFamily, column));
                }
            }
        });
        return output;
    }

    /**
     * Initialization method: HBase client configuration
     */
    private void setHBaseConfiguration() {
        configuration = createHBaseNewBasicConf();
        maxScanPageSize = hBaseConnectorUtil.getMaxScanPageSize();
    }

    private Configuration createHBaseNewBasicConf() {
        Configuration c = HBaseConfiguration.create();
        c.setBoolean("hbase.cluster.distributed", hBaseConnectorUtil.getClusterDistributed());
        c.set("hbase.master", hBaseConnectorUtil.getMaster());
        c.set("hbase.master.hostname", hBaseConnectorUtil.getMasterHostname());
        c.setInt("hbase.master.info.port", hBaseConnectorUtil.getMasterInfoPort());
        c.setInt("hbase.master.port", hBaseConnectorUtil.getMasterPort());
        c.setInt("hbase.regionserver.info.port", hBaseConnectorUtil.getRegionserverInfoPort());
        c.setInt("hbase.regionserver.port", hBaseConnectorUtil.getRegionserverPort());
        c.set("hbase.rootdir", hBaseConnectorUtil.getRootdir());
        c.set("hbase.zookeeper.quorum", hBaseConnectorUtil.getZookeeperQuorum());
        return c;
    }


    @Override
    public boolean tableExists(String tableName) throws IOException {
        AtomicBoolean exists = new AtomicBoolean(false);
        Runnable r = () -> {
            try {
                exists.set(admin.tableExists(TableName.valueOf(tableName)));
            } catch (IOException e) {
                HBaseConnectorSystemServiceImpl.this.getLog().error(e.getMessage(), e);
            }
        };
        wrapInsideHBaseConnectorClassLoader(r);
        return exists.get();
    }

    @Reference
    public void setHBaseConnectorUtil(HBaseConnectorUtil hBaseConnectorUtil) {
        this.hBaseConnectorUtil = hBaseConnectorUtil;
    }

    @Reference
    public void setPermissionSystemApi(PermissionSystemApi permissionSystemApi) {
        this.permissionSystemApi = permissionSystemApi;
    }

    @Override
    public void scanResults(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit, Consumer<Result> consumerFunction) {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader thisClassLoader = SimpleSaslAuthenticationProvider.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(thisClassLoader);
        try {
            ResultScanner scanner = getScanner(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit);
            for (Result result : scanner) {
                consumerFunction.accept(result);
            }
        } catch (Throwable t) {
            getLog().error(t.getMessage(), t);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
    }

    @Override
    public void iterateOverResults(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit, BiConsumer<Result, Boolean> consumerFunction) {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader thisClassLoader = SimpleSaslAuthenticationProvider.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(thisClassLoader);
        try {
            ResultScanner scanner = getScanner(tableName, columns, rowKeyLowerBound, rowKeyUpperBound, limit);
            Iterator<Result> it = scanner.iterator();
            while (it.hasNext()) {
                Result r = it.next();
                boolean hasMoreElements = it.hasNext();
                consumerFunction.accept(r, hasMoreElements);
            }
        } catch (Throwable t) {
            getLog().error(t.getMessage(), t);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
    }

    private void wrapInsideHBaseConnectorClassLoader(Runnable r) {
        ClassLoader karafClassLoader = Thread.currentThread().getContextClassLoader();
        ClassLoader thisClassLoader = SimpleSaslAuthenticationProvider.class.getClassLoader();
        Thread.currentThread().setContextClassLoader(thisClassLoader);
        try {
            r.run();
        } catch (Exception e) {
            getLog().error(e.getMessage(), e);
        } finally {
            Thread.currentThread().setContextClassLoader(karafClassLoader);
        }
    }

}
