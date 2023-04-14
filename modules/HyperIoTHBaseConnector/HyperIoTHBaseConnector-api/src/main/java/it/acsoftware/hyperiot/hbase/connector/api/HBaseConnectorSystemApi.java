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

package it.acsoftware.hyperiot.hbase.connector.api;

import it.acsoftware.hyperiot.base.api.HyperIoTBaseSystemApi;
import it.acsoftware.hyperiot.base.exception.HyperIoTUnauthorizedException;
import org.apache.hadoop.hbase.client.Result;
import org.apache.hadoop.hbase.client.ResultScanner;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.function.BiConsumer;
import java.util.function.Consumer;

/**
 * @author Aristide Cittadino Interface component for HBaseConnectorSystemApi. This
 * interface defines methods for additional operations.
 */
public interface HBaseConnectorSystemApi extends HyperIoTBaseSystemApi {

    /**
     * It runs a task inside dedicated thread pool for hbase tasks
     *
     * @param runnableTask
     */
    void executeTask(Runnable runnableTask);

    /**
     * It checks if there's an active connection
     *
     * @throws IOException IOException
     */
    void checkConnection() throws IOException;

    /**
     * It creates HBase Table
     *
     * @param tableName      Table name
     * @param columnFamilies HBase table column families
     * @throws IOException IOException
     */
    void createTable(String tableName, List<String> columnFamilies) throws IOException;

    /**
     * It creates HBase Table
     *
     * @param tableName      Table name
     * @param columnFamilies HBase table column families
     * @throws IOException IOException
     */
    void createTableAsync(String tableName, List<String> columnFamilies) throws IOException;

    /**
     * It deletes data
     *
     * @param tableName Table name
     * @param rowKey    Row key
     * @throws IOException IOException
     */
    void deleteData(String tableName, String rowKey) throws IOException;

    /**
     * It deletes data
     *
     * @param tableName Table name
     * @param rowKey    Row key
     * @throws IOException IOException
     */
    void deleteDataAsync(String tableName, String rowKey);

    /**
     * It disables HBase table
     *
     * @param tableName Table name
     * @throws IOException IOException
     */
    void disableTable(String tableName) throws IOException;

    /**
     * It disables HBase table
     *
     * @param tableName Table name
     * @throws IOException IOException
     */
    void disableTableAsync(String tableName);

    /**
     * It disables and drops HBase table
     *
     * @param tableName Table name
     * @throws IOException                   IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void disableAndDropTable(String tableName) throws IOException, HyperIoTUnauthorizedException;

    /**
     * It disables and drops HBase table
     *
     * @param tableName Table name
     * @throws IOException                   IOException
     * @throws HyperIoTUnauthorizedException Unauthorized operation
     */
    void disableAndDropTableAsync(String tableName);


    /**
     * It drops HBase table
     *
     * @param tableName Table name
     * @throws IOException IOException
     */
    void dropTable(String tableName) throws IOException;

    /**
     * It drops HBase table
     *
     * @param tableName Table name
     * @throws IOException IOException
     */
    void dropTableAsync(String tableName);

    /**
     * It enables HBase table
     *
     * @param tableName Table name
     * @throws IOException IOException
     */
    void enableTable(String tableName) throws IOException;

    /**
     * It returns an HBase scanner, setting on it three filters:
     * - row keys greater or equal than a lower bound
     * - row keys less or equal than an upper bound
     * - client max scanning
     *
     * @param tableName        HBase table name which derive a scanner from
     * @param columns          HBase table columns
     * @param rowKeyLowerBound Row key lower bound
     * @param rowKeyUpperBound Row key upper bound
     * @param limit            row scan limit.
     *                         If negative, scan without limit;
     *                         if equals to 0, allow scan up to HYPERIOT_HBASE_CONNECTOR_PROPERTY_CLIENT_SCANNER_MAX_RESULT_SIZE env variable;
     *                         otherwise, allow scan with the specified limit, but not exceeding HYPERIOT_HBASE_CONNECTOR_PROPERTY_CLIENT_SCANNER_MAX_RESULT_SIZE
     * @return A ResultScanner on table
     * @throws IOException IOException
     */
    ResultScanner getScanner(String tableName, Map<byte[], List<byte[]>> columns,
                             byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit)
            throws IOException;

    /**
     * It inserts data
     *
     * @param tableName    Table name
     * @param rowKey       Row key
     * @param columnFamily Column Family
     * @param column       Column
     * @param cellValue    Value to insert
     * @throws IOException IOException
     */
    void insertData(String tableName, String rowKey, String columnFamily, String column, String cellValue)
            throws IOException;

    /**
     * It inserts data
     *
     * @param tableName    Table name
     * @param rowKey       Row key
     * @param columnFamily Column Family
     * @param column       Column
     * @param cellValue    Value to insert
     * @throws IOException IOException
     */
    void insertDataAsync(String tableName, String rowKey, String columnFamily, String column, String cellValue);

    /**
     * It counts all rows between row key lower bound and row key upper bound
     *
     * @param tableName
     * @param columnFamily
     * @param column
     * @param rowKeyLowerBound
     * @param rowKeyUpperBound
     * @return
     * @throws Throwable
     */
    long rowCount(String tableName, byte[] columnFamily, byte[] column, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws Throwable;

    /**
     * It counts all rows between row key lower bound and row key upper bound
     *
     * @param tableName
     * @param columns
     * @param rowKeyLowerBound
     * @param rowKeyUpperBound
     * @return
     * @throws Throwable
     */
    long rowCount(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws Throwable;

    /**
     * It scans HBase table
     *
     * @param tableName        table name
     * @param columnFamily     column family
     * @param column           column
     * @param rowKeyLowerBound rowKeyLowerBound
     * @param rowKeyUpperBound rowKeyUpperBound
     * @return List<byte [ ]>
     * @throws IOException IOException
     */
    @SuppressWarnings("unused")
    List<byte[]> scan(String tableName, byte[] columnFamily, byte[] column,
                      byte[] rowKeyLowerBound, byte[] rowKeyUpperBound) throws IOException;

    /**
     * It scans HBase table
     *
     * @param tableName        table name
     * @param columns          columns
     * @param rowKeyLowerBound rowKeyLowerBound
     * @param rowKeyUpperBound rowKeyUpperBound
     * @return List<Result>
     * @throws IOException IOException
     */
    @SuppressWarnings("unused")
    List<Result> scanWithCompleteResult(String tableName, Map<byte[], List<byte[]>> columns,
                                        byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit) throws IOException;

    /**
     * It scans HBase table. TODO: more generic than the above one, use this wherever
     *
     * @param tableName        table name
     * @param columns          map where keys are column families and values are columns belonging to each family
     * @param rowKeyLowerBound row key lower bound
     * @param rowKeyUpperBound row key upper bound
     * @param limit            scan limit
     * @return map
     * @throws IOException IOException
     */
    Map<byte[], Map<byte[], Map<byte[], byte[]>>> scan(String tableName, Map<byte[], List<byte[]>> columns,
                                                       byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit)
            throws IOException;

    /**
     * It checks if table exists
     *
     * @param tableName Table name
     * @return True if it exists, false otherwise
     * @throws IOException IOException
     */
    @SuppressWarnings("unused")
    boolean tableExists(String tableName) throws IOException;

    /**
     * Offer a consumer function to iterate over results
     * @param tableName
     * @param columns
     * @param rowKeyLowerBound
     * @param rowKeyUpperBound
     * @param limit
     * @param consumerFunction
     */
    void scanResults(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit, Consumer<Result> consumerFunction);

    /**
     * Iterates over result with info about elements
     * @param tableName
     * @param columns
     * @param rowKeyLowerBound
     * @param rowKeyUpperBound
     * @param limit
     * @param consumerFunction
     */
    void iterateOverResults(String tableName, Map<byte[], List<byte[]>> columns, byte[] rowKeyLowerBound, byte[] rowKeyUpperBound, int limit, BiConsumer<Result, Boolean> consumerFunction);

}