package it.acsoftware.hyperiot.hbase.connector.api;

public interface HBaseConnectorUtil {

    long getAwaitTermination();

    boolean getClusterDistributed();

    int getCorePoolSize();

    long getKeepAliveTime();

    String getMaster();

    String getMasterHostname();

    int getMasterInfoPort();

    int getMasterPort();

    int getMaximumPoolSize();

    int getMaxScanPageSize();

    int getRegionserverInfoPort();

    int getRegionserverPort();

    String getRootdir();

    String getZookeeperQuorum();

}
