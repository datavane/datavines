# PostgreSQL配置

<cite>
**本文档引用的文件**
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java)
- [PostgreSqlConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlConnector.java)
- [PostgreSqlConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlConfigBuilder.java)
- [PostgreSqlTypeConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlTypeConverter.java)
- [PostgreSqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlParameterConverter.java)
- [JdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java)
- [JdbcConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConfigBuilder.java)
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)
</cite>

## 目录
1. [JDBC连接字符串格式](#jdbc连接字符串格式)
2. [连接参数配置](#连接参数配置)
3. [SSL配置方法](#ssl配置方法)
4. [连接池参数设置](#连接池参数设置)
5. [PostgreSQL特有配置选项](#postgresql特有配置选项)
6. [性能调优建议](#性能调优建议)
7. [连接器连通性和性能测试](#连接器连通性和性能测试)

## JDBC连接字符串格式

PostgreSQL连接器的JDBC连接字符串遵循标准格式，由协议、主机、端口和数据库名称组成。根据代码实现，PostgreSQL连接器的地址格式为：

```
jdbc:postgresql://host:port/database
```

其中：
- `jdbc:postgresql://` 是PostgreSQL的JDBC协议前缀
- `host` 是数据库服务器的主机名或IP地址
- `port` 是数据库服务器的端口号（默认为5432）
- `database` 是要连接的数据库名称

在代码中，`PostgreSqlDataSourceInfo`类的`getAddress()`方法实现了这一格式的构建：

```java
@Override
public String getAddress() {
    return "jdbc:postgresql://"+getHost()+":"+getPort();
}
```

完整的JDBC URL通过`getJdbcUrl()`方法构建，该方法在基类`BaseJdbcDataSourceInfo`中实现，将地址、数据库名称和连接参数组合在一起。

**本节来源**
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java#L34-L37)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java#L96-L103)

## 连接参数配置

PostgreSQL连接器支持多种连接参数，这些参数可以通过URL查询字符串的形式附加到JDBC连接字符串后面。参数之间使用`&`符号分隔。

### 基本连接参数

根据`JdbcConfigBuilder`和`PostgreSqlConfigBuilder`类的实现，PostgreSQL连接器需要以下基本参数：

- **host**: 数据库服务器地址
- **port**: 数据库服务器端口
- **database**: 要连接的数据库名称
- **schema**: 模式名称（PostgreSQL特有，必填）
- **user**: 用户名
- **password**: 密码
- **properties**: 其他连接属性，格式为`key=value&key1=value1`

PostgreSQL连接器特别要求模式（schema）参数是必需的，这在`PostgreSqlConfigBuilder`类中通过设置验证规则实现：

```java
@Override
protected InputParam getSchemaInput(boolean isEn) {
    return getInputParam("schema",
            isEn ? "schema" : "模式",
            isEn ? "please enter schema" : "请填入模式", 1,
            Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter schema" : "请填入模式").build(), null);
}
```

### 连接参数处理

连接参数通过`filterProperties`方法进行处理，该方法会过滤掉敏感参数`autoDeserialize=true`，以防止潜在的安全风险：

```java
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
```

**本节来源**
- [PostgreSqlConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlConfigBuilder.java#L24-L30)
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java#L55-L75)
- [JdbcConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConfigBuilder.java#L77-L129)

## SSL配置方法

虽然在提供的代码片段中没有直接显示SSL配置的具体实现，但PostgreSQL JDBC驱动支持多种SSL连接模式。可以通过在连接参数中添加SSL相关属性来配置SSL连接。

### SSL连接参数

PostgreSQL JDBC驱动支持以下SSL相关参数：

- **ssl**: 是否启用SSL连接（true/false）
- **sslmode**: SSL模式，可选值包括：
  - `disable`: 禁用SSL
  - `allow`: 首先尝试非SSL连接，如果失败则尝试SSL
  - `prefer`（默认）: 首先尝试SSL连接，如果失败则尝试非SSL
  - `require`: 必须使用SSL连接，不验证服务器证书
  - `verify-ca`: 必须使用SSL连接，并验证服务器证书是否由受信任的CA签发
  - `verify-full`: 必须使用SSL连接，并验证服务器证书是否由受信任的CA签发，且主机名与证书中的主机名匹配

- **sslcert**: 客户端证书文件路径
- **sslkey**: 客户端密钥文件路径
- **sslrootcert**: 信任的CA证书文件路径
- **sslfactory**: SSL工厂类

### 配置示例

在连接参数中配置SSL连接：

```
ssl=true&sslmode=require&sslcert=/path/to/client-cert.pem&sslkey=/path/to/client-key.pem&sslrootcert=/path/to/ca-cert.pem
```

由于PostgreSQL连接器的`filterProperties`方法会过滤掉包含`autoDeserialize=true`的参数，建议在配置SSL时避免使用可能触发安全过滤的参数。

**本节来源**
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java#L55-L75)

## 连接池参数设置

在提供的代码库中，没有直接显示连接池的具体配置，但可以通过JDBC连接参数来配置连接池行为。PostgreSQL JDBC驱动本身不提供连接池功能，但可以与第三方连接池库（如HikariCP、Apache DBCP等）集成。

### 连接池相关参数

虽然连接池通常由外部库管理，但可以通过JDBC参数影响连接行为：

- **loginTimeout**: 连接超时时间（秒）
- **socketTimeout**: Socket操作超时时间（秒）
- **connectTimeout**: 连接建立超时时间（秒）
- **cancelSignalTimeout**: 取消信号超时时间（秒）

### 连接验证

PostgreSQL连接器使用自定义的验证查询来测试连接：

```java
@Override
public String getValidationQuery() {
    return "SELECT 'x'";
}
```

这与许多数据库使用的`SELECT 1`不同，使用字符串`'x'`作为验证查询。这个查询非常轻量，适合用于连接池的连接验证。

### 连接管理

在`JdbcConnector`类中，连接的获取和释放通过`dataSourceClient`完成：

```java
protected Connection getConnection(String dataSourceParam, Map<String,String> param) throws SQLException {
    return dataSourceClient.getConnection(JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(param)));
}
```

连接在使用后会被正确释放：

```java
JdbcDataSourceUtils.releaseConnection(connection);
```

**本节来源**
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java#L78-L80)
- [JdbcConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConnector.java#L62-L64)
- [JdbcConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConnector.java#L83-L84)

## PostgreSQL特有配置选项

PostgreSQL连接器实现了一些特定于PostgreSQL的配置和功能。

### 模式搜索路径配置

PostgreSQL使用模式（schema）作为数据库对象的命名空间。在`PostgreSqlConfigBuilder`中，模式被设置为必填参数，这反映了PostgreSQL对模式的重视：

```java
@Override
protected InputParam getSchemaInput(boolean isEn) {
    return getInputParam("schema",
            isEn ? "schema" : "模式",
            isEn ? "please enter schema" : "请填入模式", 1,
            Validate.newBuilder().setRequired(true).setMessage(isEn ? "please enter schema" : "请填入模式").build(), null);
}
```

在连接时，模式信息被用于元数据查询。当获取表列表时，会使用指定的模式：

```java
String catalog;
String schema;

if (StringUtils.isNotEmpty(paramMap.get(CATALOG))) {
    catalog = paramMap.get(CATALOG);
    schema = param.getDatabase();
} else {
    catalog = param.getDatabase();
    schema = StringUtils.isEmptyOrNullStr(paramMap.get(SCHEMA))  ? null : paramMap.get(SCHEMA);
}
```

### 数据类型处理

`PostgreSqlTypeConverter`类负责将PostgreSQL的原生数据类型转换为系统内部的数据类型。PostgreSQL支持丰富的数据类型，包括一些特有的类型：

```java
@Override
public DataType convert(String originType) {
    if (StringUtils.isEmpty(originType)) {
        throw new UnsupportedOperationException("sql type id null error");
    }
    switch (originType.toUpperCase()) {
        case "INT4":
        case "INT2":
        case "OID":
        case "SERIAL":
            return DataType.INT_TYPE;
        case "BIGSERIAL":
        case "INT8":
            return DataType.LONG_TYPE;
        case "BOOL":
            return DataType.BOOLEAN_TYPE;
        case "FLOAT8":
        case "FLOAT4":
        case "REAL":
            return DataType.FLOAT_TYPE;
        case "NUMBER":
        case "MONEY":
            return DataType.DOUBLE_TYPE;
        case "TIMESTAMPTZ":
            return DataType.TIMESTAMP_TYPE;
        case "TIMETZ":
            return DataType.TIME_TYPE;
        case "BPCHAR":
        case "UUID":
        case "JSONB":
        case "XML":
            return DataType.STRING_TYPE;
        case "NUMERIC":
            return DataType.BIG_DECIMAL_TYPE;
        case "CIDR":
        case "INET":
        case "JSONPATH":
        case "CIRCLE":
        case "POINT":
        case "LINE":
        case "BOX":
        case "PATH":
        case "POLYGON":
        case "LSEG":
        case "VARBIT":
            return DataType.OBJECT;
        default:
            return super.convert(originType);
    }
}
```

特别值得注意的是：
- `JSONB` 和 `JSONPATH` 类型被映射为字符串类型
- `UUID`、`CIDR`、`INET` 等网络相关类型被映射为字符串类型
- 几何类型（`POINT`、`LINE`、`BOX`、`PATH`、`POLYGON`、`CIRCLE`）被映射为对象类型
- `VARBIT`（可变长度位串）被映射为对象类型

### 数组类型处理

虽然在提供的代码中没有直接显示数组类型的处理，但PostgreSQL的JDBC驱动原生支持数组类型。可以通过标准的JDBC API来处理数组：

```java
// 获取数组
Array array = resultSet.getArray("array_column");
Object[] values = (Object[]) array.getArray();

// 设置数组参数
Connection connection = dataSource.getConnection();
java.sql.Array sqlArray = connection.createArrayOf("text", new String[]{"a", "b", "c"});
preparedStatement.setArray(1, sqlArray);
```

### JSON类型支持

PostgreSQL的`JSONB`类型被映射为字符串类型，这意味着JSON数据在系统中作为字符串处理。对于需要JSON处理的场景，可以在应用层进行JSON解析和操作。

**本节来源**
- [PostgreSqlConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlConfigBuilder.java#L24-L30)
- [PostgreSqlTypeConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlTypeConverter.java#L24-L71)
- [JdbcConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConnector.java#L113-L119)

## 性能调优建议

基于代码实现和PostgreSQL最佳实践，以下是性能调优建议。

### 连接复用

连接复用是提高性能的关键。系统通过`dataSourceClient`管理连接，确保连接可以被复用：

```java
protected Connection getConnection(String dataSourceParam, Map<String,String> param) throws SQLException {
    return dataSourceClient.getConnection(JdbcDataSourceInfoManager.getDatasourceInfo(dataSourceParam, getDatasourceInfo(param)));
}
```

建议：
- 使用连接池来管理数据库连接
- 设置合理的连接池大小，避免过多的连接导致数据库资源耗尽
- 配置连接池的最小和最大连接数，根据应用负载进行调整

### 批量操作

对于大量数据的插入或更新操作，使用批量操作可以显著提高性能：

```java
// 批量插入示例
PreparedStatement pstmt = connection.prepareStatement("INSERT INTO table (col1, col2) VALUES (?, ?)");
for (Data data : dataList) {
    pstmt.setString(1, data.getCol1());
    pstmt.setString(2, data.getCol2());
    pstmt.addBatch();
}
pstmt.executeBatch();
```

### 查询超时设置

设置查询超时可以防止长时间运行的查询影响系统性能：

```java
// 设置查询超时
statement.setQueryTimeout(30); // 30秒超时
```

或者通过连接参数设置：

```
socketTimeout=30&connectTimeout=10
```

### 其他性能优化

1. **使用PreparedStatement**: 对于重复执行的SQL语句，使用PreparedStatement可以提高性能并防止SQL注入。

2. **合理使用索引**: 确保查询中使用的列有适当的索引。

3. **避免SELECT ***: 只选择需要的列，减少数据传输量。

4. **使用合适的事务隔离级别**: 根据业务需求选择合适的隔离级别，避免不必要的锁竞争。

5. **监控和分析慢查询**: 使用PostgreSQL的`pg_stat_statements`扩展来监控和分析慢查询。

**本节来源**
- [JdbcConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConnector.java#L62-L64)

## 连接器连通性和性能测试

系统提供了测试连接的功能，可以通过`testConnect`方法来验证连接器的连通性。

### 连通性测试

`testConnect`方法实现了连接测试功能：

```java
@Override
public ConnectorResponse testConnect(TestConnectionRequestParam param) {
    Map<String,String> paramMap = JSONUtils.toMap(param.getDataSourceParam());
    BaseJdbcDataSourceInfo dataSourceInfo = getDatasourceInfo(paramMap);
    dataSourceInfo.loadClass();

    try (Connection con = DriverManager.getConnection(dataSourceInfo.getJdbcUrl(), dataSourceInfo.getUser(), dataSourceInfo.getPassword())) {
        boolean result = con != null;
        if (result) {
            con.close();
        }
        return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(result).build();
    } catch (SQLException e) {
        logger.error("test connect error, param is {} :", JSONUtils.toJsonString(param), e);
    }

    return ConnectorResponse.builder().status(ConnectorResponse.Status.SUCCESS).result(false).build();
}
```

测试过程包括：
1. 解析连接参数
2. 加载JDBC驱动类
3. 尝试建立数据库连接
4. 如果连接成功，立即关闭连接
5. 返回测试结果

### 性能测试

虽然没有直接的性能测试方法，但可以通过以下方式评估连接器性能：

1. **连接建立时间**: 测量`getConnection`方法的执行时间。
2. **查询响应时间**: 测量执行查询的响应时间。
3. **并发性能**: 测试多线程并发访问时的性能表现。

### 测试建议

1. **连接测试**: 在配置新的数据源后，首先使用测试连接功能验证配置是否正确。
2. **超时测试**: 验证连接超时和查询超时设置是否生效。
3. **SSL连接测试**: 如果配置了SSL，确保SSL连接能够正常建立。
4. **长时间运行测试**: 进行长时间运行测试，确保连接不会意外断开。

**本节来源**
- [JdbcConnector.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConnector.java#L196-L212)
- [PostgreSqlDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-postgresql\src\main\java\io\datavines\connector\plugin\PostgreSqlDataSourceInfo.java#L149-L152)