package io.datavines.engine.jdbc.api;

import io.datavines.common.config.CheckResult;
import io.datavines.common.config.Config;
import io.datavines.engine.api.env.Execution;
import io.datavines.engine.api.env.RuntimeEnvironment;

import java.sql.Connection;
import java.sql.SQLException;

/**
 * 
 */
public class JdbcRuntimeEnvironment implements RuntimeEnvironment {

    private Connection sourceConnection;

    private Connection metadataConnection;

    @Override
    public void prepare() {

    }

    @Override
    public Execution getExecution() {
        return new JdbcExecution(this);
    }

    @Override
    public void setConfig(Config config) {

    }

    @Override
    public Config getConfig() {
        return null;
    }

    @Override
    public CheckResult checkConfig() {
        return null;
    }

    public Connection getSourceConnection() {
        return sourceConnection;
    }

    public void setSourceConnection(Connection sourceConnection) {
        this.sourceConnection = sourceConnection;
    }

    public Connection getMetadataConnection() {
        return metadataConnection;
    }

    public void setMetadataConnection(Connection metadataConnection) {
        this.metadataConnection = metadataConnection;
    }

    public void close() throws SQLException {
        if(sourceConnection != null) {
            sourceConnection.close();
        }

        if(metadataConnection != null) {
            metadataConnection.close();
        }
    }
}
