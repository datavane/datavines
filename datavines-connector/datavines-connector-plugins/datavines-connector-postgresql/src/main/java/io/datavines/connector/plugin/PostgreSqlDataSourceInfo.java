package io.datavines.connector.plugin;

import io.datavines.connector.plugin.datasource.BaseDataSourceInfo;
import io.datavines.connector.plugin.datasource.ConnectionInfo;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PostgreSqlDataSourceInfo extends BaseDataSourceInfo {

    private final Logger logger = LoggerFactory.getLogger(PostgreSqlDataSourceInfo.class);

    public PostgreSqlDataSourceInfo(ConnectionInfo connectionInfo) {
        super(connectionInfo);
    }

    @Override
    public String getAddress() {
        return "jdbc:postgresql://"+getHost()+":"+getPort();
    }

    @Override
    public String getDriverClass() {
        return "org.postgresql.Driver";
    }

    @Override
    public String getType() {
        return "postgresql";
    }

    @Override
    protected String getSeparator() {
        return "?";
    }

    @Override
    protected String filterProperties(String other){
        if(StringUtils.isBlank(other)){
            return "";
        }

        String sensitiveParam = "autoDeserialize=true";
        if(other.contains(sensitiveParam)){
            int index = other.indexOf(sensitiveParam);
            String tmp = sensitiveParam;
            char symbol = '&';
            if(index == 0 || other.charAt(index + 1) == symbol){
                tmp = tmp + symbol;
            } else if(other.charAt(index - 1) == symbol){
                tmp = symbol + tmp;
            }
            logger.warn("sensitive param : {} in properties field is filtered", tmp);
            other = other.replace(tmp, "");
        }
        logger.debug("properties : {}", other);
        return other;
    }
}
