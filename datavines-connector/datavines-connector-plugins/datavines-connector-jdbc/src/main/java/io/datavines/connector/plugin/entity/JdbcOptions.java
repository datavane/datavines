package io.datavines.connector.plugin.entity;

import lombok.Data;

@Data
public class JdbcOptions {

    private String url;

    private String tableName;

    private String query;

    private String partitionColumn;

    private int queryTimeout;

    private int fetchSize;

    private int batchSize;
}
