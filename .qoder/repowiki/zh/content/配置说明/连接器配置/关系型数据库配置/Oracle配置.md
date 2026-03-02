# Oracle配置

<cite>
**本文档引用的文件**
- [OracleConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleConfigBuilder.java)
- [OracleDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleDataSourceInfo.java)
- [OracleParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleParameterConverter.java)
- [OracleDialect.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleDialect.java)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java)
- [JdbcConnectionInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\JdbcConnectionInfo.java)
- [JdbcDataSourceManager.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\JdbcDataSourceManager.java)
</cite>

## 目录
1. [JDBC连接字符串格式](#jdbc连接字符串格式)
2. [连接参数配置](#连接参数配置)
3. [SSL配置方法](#ssl配置方法)
4. [连接池参数设置](#连接池参数设置)
5. [Oracle特有配置选项](#oracle特有配置选项)
6. [性能调优建议](#性能调优建议)
7. [连接测试方法](#连接测试方法)

## JDBC连接字符串格式

Oracle连接器支持标准的JDBC连接字符串格式，采用Oracle Thin驱动程序进行连接。连接字符串的格式遵循Oracle官方规范，主要支持两种格式：

1. **服务名格式**：`jdbc:oracle:thin:@//host:port/service_name`
2. **SID格式**：`jdbc:oracle:thin:@host:port:SID`

在DataVines实现中，Oracle连接器使用服务名格式作为默认连接方式。连接字符串的构建通过`OracleParameterConverter`类的`getUrl`方法实现，该方法根据提供的主机、端口和SID参数生成完整的JDBC URL。

连接字符串的地址部分由`OracleDataSourceInfo`类的`getAddress`方法生成，格式为`jdbc:oracle:thin:@//host:port`。完整的JDBC URL通过`getJdbcUrl`方法构建，该方法在地址基础上附加SID和服务名。

**Section sources**
- [OracleParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleParameterConverter.java#L29-L38)
- [OracleDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleDataSourceInfo.java#L34-L35)

## 连接参数配置

Oracle连接器的配置参数通过`OracleConfigBuilder`类定义，该类继承自`JdbcConfigBuilder`并添加了Oracle特有的配置项。主要连接参数包括：

- **主机地址**（host）：数据库服务器的IP地址或主机名
- **端口**（port）：数据库监听端口，默认为1521
- **SID**（sid）：Oracle系统标识符
- **用户名**（user）：数据库连接用户名
- **密码**（password）：数据库连接密码
- **附加属性**（properties）：其他连接参数，以key=value&key1=value1格式提供

`OracleConfigBuilder`类的`build`方法负责生成配置参数的JSON表示，该方法定义了配置界面中显示的参数列表。SID参数通过`getSID`方法单独定义，该参数是必填项。

附加属性参数允许用户指定Oracle JDBC驱动的特定连接属性，这些属性将附加到JDBC URL的查询字符串部分。`BaseJdbcDataSourceInfo`类的`appendProperties`方法负责处理这些附加属性。

**Section sources**
- [OracleConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleConfigBuilder.java#L34-L63)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java#L135-L139)

## SSL配置方法

虽然代码库中没有直接显示Oracle SSL配置的具体实现，但可以通过连接参数中的附加属性来配置SSL连接。Oracle JDBC驱动支持通过连接属性配置SSL/TLS加密。

SSL配置通常通过在连接字符串的附加属性中指定以下参数来实现：

- `oracle.net.ssl_server_dn_match`：启用服务器DN匹配验证
- `oracle.net.ssl_version`：指定SSL/TLS版本
- `javax.net.ssl.trustStore`：指定信任库路径
- `javax.net.ssl.trustStorePassword`：指定信任库密码

这些SSL相关参数可以通过`properties`字段以key=value格式提供，并与其他连接参数用&符号分隔。`OracleParameterConverter`类的`getUrl`方法会将这些附加属性附加到JDBC URL的查询字符串部分。

**Section sources**
- [OracleParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleParameterConverter.java#L34-L36)

## 连接池参数设置

DataVines使用HikariCP作为数据库连接池实现，连接池的配置在`JdbcDataSourceManager`类中定义。虽然Oracle连接器本身不直接管理连接池参数，但系统级的连接池配置会影响所有数据库连接，包括Oracle。

连接池的主要参数包括：

- **最大连接池大小**：通过`setMaximumPoolSize`方法设置，默认值为10
- **驱动类名**：通过`setDriverClassName`方法设置，Oracle为`oracle.jdbc.driver.OracleDriver`
- **JDBC URL**：从数据源信息中获取
- **用户名和密码**：从数据源信息中获取

`JdbcDataSourceManager`类的`getDataSource`方法负责创建和管理连接池。当请求新的数据源连接时，该方法会检查是否存在已缓存的连接池，如果不存在则创建新的HikariDataSource实例。

连接池的配置是全局的，适用于所有使用JDBC连接的数据库类型。连接池实例通过数据源的唯一键（由`getUniqueKey`方法生成）进行缓存和管理。

**Section sources**
- [JdbcDataSourceManager.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\JdbcDataSourceManager.java#L59-L88)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java#L166-L168)

## Oracle特有配置选项

### 服务名与SID的区别

在Oracle数据库中，服务名（Service Name）和SID（System Identifier）是两种不同的数据库标识方式：

- **SID**：是数据库实例的唯一标识符，通常在数据库创建时指定
- **服务名**：是数据库服务的逻辑名称，可以由一个或多个实例提供服务

在DataVines的Oracle连接器中，SID作为主要的连接标识符。`OracleDataSourceInfo`类的`appendSid`方法负责将SID附加到JDBC URL中。如果SID参数不为空，则将其附加到地址后面。

### TNS配置

代码库中没有直接支持TNS（Transparent Network Substrate）配置。连接主要通过直接的主机、端口和SID/服务名进行，而不是使用TNS别名。这简化了配置过程，但限制了使用复杂的TNS网络配置。

### Oracle RAC配置

对于Oracle RAC（Real Application Clusters）环境，可以通过在主机参数中指定多个地址来实现连接。虽然代码中没有专门的RAC配置，但Oracle JDBC驱动本身支持RAC连接。

RAC连接可以通过在连接字符串中使用地址列表来实现，例如：
`jdbc:oracle:thin:@(DESCRIPTION=(ADDRESS_LIST=(ADDRESS=(PROTOCOL=TCP)(HOST=host1)(PORT=1521))(ADDRESS=(PROTOCOL=TCP)(HOST=host2)(PORT=1521)))(CONNECT_DATA=(SERVICE_NAME=service_name)))`

然而，当前的`OracleDataSourceInfo`实现主要支持简单的单主机连接格式。

**Section sources**
- [OracleDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleDataSourceInfo.java#L68-L75)

## 性能调优建议

### 连接复用

DataVines通过连接池实现连接复用，避免了频繁创建和销毁数据库连接的开销。`JdbcDataSourceManager`类维护一个连接池缓存，通过数据源的唯一键来识别和重用现有的连接池。

连接复用的实现基于`getUniqueKey`方法生成的MD5哈希值，该值由主机、端口、数据库、用户名等参数组合生成。相同的连接参数将使用同一个连接池实例。

### 批量操作

虽然代码中没有直接显示批量操作的配置，但可以通过JDBC的批量处理功能来优化大量数据的插入和更新操作。建议在执行大量数据操作时使用PreparedStatement的addBatch和executeBatch方法。

### 查询超时设置

查询超时可以通过JDBC连接的`setQueryTimeout`方法设置。虽然在提供的代码中没有直接显示超时设置，但可以在执行查询时通过JdbcTemplate或类似的工具类来配置查询超时。

Oracle JDBC驱动支持以下超时相关参数：
- `oracle.jdbc.ReadTimeout`：读取超时（毫秒）
- `oracle.net.CONNECT_TIMEOUT`：连接超时（毫秒）

这些参数可以通过连接字符串的附加属性进行配置。

**Section sources**
- [JdbcDataSourceManager.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\JdbcDataSourceManager.java#L71-L92)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java#L166-L168)

## 连接测试方法

Oracle连接器的连通性测试通过`OracleDataSourceInfo`类的`getConnection`方法实现。该方法使用标准的JDBC流程来测试连接：

1. 加载Oracle JDBC驱动类
2. 使用JDBC URL、用户名和密码创建数据库连接
3. 返回连接对象或抛出异常

连接测试的验证查询由`getValidationQuery`方法定义，对于Oracle数据库，使用`Select 1 FROM DUAL`作为验证查询。DUAL是Oracle中的一个特殊单行表，常用于测试连接和执行函数。

`BaseJdbcDataSourceInfo`类的`getConnection`方法提供了通用的连接测试逻辑，该方法被所有JDBC数据源类型继承和使用。连接测试过程中会加载驱动类并尝试建立实际的数据库连接。

**Section sources**
- [OracleDataSourceInfo.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-oracle\src\main\java\io\datavines\connector\plugin\OracleDataSourceInfo.java#L53-L55)
- [BaseJdbcDataSourceInfo.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\BaseJdbcDataSourceInfo.java#L149-L152)