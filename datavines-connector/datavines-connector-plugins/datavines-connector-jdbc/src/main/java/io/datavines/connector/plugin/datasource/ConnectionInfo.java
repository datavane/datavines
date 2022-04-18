package io.datavines.connector.plugin.datasource;

import io.datavines.common.utils.Md5Utils;
import lombok.Data;

/**
 * ConnectionInfo
 */
@Data
public class ConnectionInfo {

    /**
     * user
     */
    protected String user;

    /**
     * user password
     */
    protected String password;

    /**
     * data source address
     */
    private String host;

    /**
     * datasource port
     */
    private int port;

    /**
     * database name
     */
    private String database;

    /**
     * properties
     */
    private String properties;

    @Override
    public String toString() {
        return host.trim() +
                "&" + port +
                "&" + database.trim() +
                "&" + user.trim() +
                "&" + password.trim() +
                "&" + properties;
    }

    public String getUniqueKey() {
        return Md5Utils.getMd5(toString(), false);
    }
}
