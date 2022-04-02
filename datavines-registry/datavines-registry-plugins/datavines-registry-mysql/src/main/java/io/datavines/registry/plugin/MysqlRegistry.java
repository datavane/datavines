package io.datavines.registry.plugin;

import io.datavines.common.exception.DataVinesException;
import io.datavines.registry.api.ConnectionListener;
import io.datavines.registry.api.Registry;
import io.datavines.registry.api.ServerInfo;
import io.datavines.registry.api.SubscribeListener;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.List;
import java.util.Properties;

public class MysqlRegistry implements Registry {

    private static final Logger logger = LoggerFactory.getLogger(MysqlRegistry.class);

    private MysqlMutex mysqlMutex;

    private MysqlServerStateManager mysqlServerStateManager;

    @Override
    public void init(Properties properties) throws Exception {

        Connection connection = ConnectionUtils.getConnection(properties);

        if (connection == null){
            throw new Exception("can not create connection");
        }

        try {
            mysqlMutex = new MysqlMutex(connection, properties);
            mysqlServerStateManager = new MysqlServerStateManager(connection, properties);
        } catch (SQLException exception) {
            logger.error("init mysql mutex error: " + exception.getLocalizedMessage());
        }
    }

    @Override
    public boolean acquire(String key, long timeout){
        try {
            return mysqlMutex.acquire(key, timeout);
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public boolean release(String key){
        try {
            return mysqlMutex.release();
        } catch (Exception e) {
            logger.warn("acquire lock error: ", e);
            return false;
        }
    }

    @Override
    public void subscribe(String key, SubscribeListener subscribeListener) {
        try {
            mysqlServerStateManager.registry(subscribeListener);
        } catch (Exception e){
            e.printStackTrace();
        }

    }

    @Override
    public void unSubscribe(String key) {
        try {
            mysqlServerStateManager.unRegistry();
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    @Override
    public void addConnectionListener(ConnectionListener connectionListener) {

    }

    @Override
    public List<ServerInfo> getActiveServerList() {
        return mysqlServerStateManager.getActiveServerList();
    }
}
