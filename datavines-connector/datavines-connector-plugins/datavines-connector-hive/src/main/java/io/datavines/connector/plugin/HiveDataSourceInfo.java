package io.datavines.connector.plugin;

import io.datavines.connector.plugin.datasource.BaseDataSourceInfo;
import io.datavines.connector.plugin.datasource.ConnectionInfo;

public class HiveDataSourceInfo extends BaseDataSourceInfo {

    public HiveDataSourceInfo(ConnectionInfo connectionInfo) {
        super(connectionInfo);
    }

    @Override
    public String getAddress() {
        return "jdbc:hive2://"+getHost()+":"+getPort();
    }

    @Override
    public String getDriverClass() {
        return "org.apache.hive.jdbc.HiveDriver";
    }

    @Override
    public String getType() {
        return "hive";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

}
