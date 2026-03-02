# Presto配置

<cite>
**本文档引用文件**  
- [PrestoConfigBuilder.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoConfigBuilder.java)
- [PrestoConnector.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoConnector.java)
- [PrestoDataSourceInfo.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceInfo.java)
- [PrestoParameterConverter.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoParameterConverter.java)
- [ConfigConstants.java](file://datavines-common/src/main/java/io/datavines/common/ConfigConstants.java)
- [BaseJdbcDataSourceInfo.java](file://datavines-common/src/main/java/io/datavines/common/datasource/jdbc/BaseJdbcDataSourceInfo.java)
- [KerberosUtils.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-hive/src/main/java/io/datavines/connector/plugin/KerberosUtils.java)
</cite>

## 目录
1. [Presto连接参数配置](#presto连接参数配置)  
2. [认证配置方法](#认证配置方法)  
3. [SSL/TLS加密连接配置](#ssltls加密连接配置)  
4. [高级配置选项](#高级配置选项)  
5. [性能调优建议](#性能调优建议)  
6. [连接测试方法](#连接测试方法)

## Presto连接参数配置

Presto连接器的配置主要包括Coordinator地址、端口、目录名称（catalog）、模式名称（schema）等核心参数。这些参数通过JDBC URL格式进行组织，确保与Presto集群的正确通信。

### 核心连接参数

Presto连接的核心参数包括：

- **主机地址（host）**：Presto Coordinator节点的IP地址或主机名
- **端口（port）**：Presto服务监听的端口号，默认为8080
- **目录名称（catalog）**：Presto中的数据源目录，如hive、mysql等
- **数据库/模式名称（database）**：指定要连接的具体数据库或模式

JDBC连接URL的生成遵循以下格式：
- 当指定数据库时：`jdbc:presto://<host>:<port>/<catalog>/<database>`
- 当未指定数据库时：`jdbc:presto://<host>:<port>/<catalog>`

这些参数在`PrestoDataSourceInfo`类中通过`getJdbcUrl()`方法构建，使用`StringBuilder`依次拼接地址、目录、数据库和附加属性。

### 参数映射与验证

`PrestoConfigBuilder`类负责定义配置参数的输入表单和验证规则：
- `catalog`字段为必填项，提示信息为"请填入目录类型"
- `database`字段为可选项，提示信息为"请填入数据库"
- `password`字段为可选项，用于基本认证

参数的键值对存储在`Map<String, String>`结构中，并通过`BaseJdbcDataSourceInfo`基类提供的方法进行访问和处理。

**本节来源**  
- [PrestoDataSourceInfo.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceInfo.java#L30-L46)
- [PrestoConfigBuilder.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoConfigBuilder.java#L24-L48)
- [ConfigConstants.java](file://datavines-common/src/main/java/io/datavines/common/ConfigConstants.java#L95-L98)

## 认证配置方法

Presto连接器支持多种认证方式，包括基本认证、Kerberos认证等，通过JDBC连接属性进行配置。

### 基本认证（Basic Authentication）

基本认证通过在连接属性中设置用户名和密码实现。在`PrestoConnector`类的`testConnect`方法中，通过`Properties`对象设置认证信息：
- 使用`USER`常量作为用户名键
- 使用`PASSWORD`常量作为密码键
- 将这些属性附加到JDBC URL的查询参数中

```java
properties.setProperty(USER, dataSourceInfo.getUser());
if (StringUtils.isNotEmpty(dataSourceInfo.getPassword())) {
    properties.setProperty(PASSWORD, dataSourceInfo.getPassword());
}
```

### Kerberos认证

虽然Presto连接器本身未直接实现Kerberos认证逻辑，但系统通过共享的Kerberos工具类支持此功能。从`KerberosUtils`类可以看出，系统支持以下Kerberos相关配置：
- **keytab文件路径**：通过`KEYTAB_FILE`常量定义
- **主体名称（Principal）**：通过`KEYTAB_PRINCIPAL`常量定义
- **krb5.conf文件路径**：通过`KRB5_CONF`常量定义

这些配置项存储在数据源参数映射中，可通过`BaseJdbcDataSourceInfo`类的相应getter方法访问。

### JWT令牌认证

系统架构支持通过`PROPERTIES`参数传递自定义连接属性，这为JWT令牌认证提供了可能性。用户可以将JWT令牌作为连接属性直接附加到JDBC URL中，例如：
```
jdbc:presto://host:port/catalog?accessToken=your.jwt.token.here
```

**本节来源**  
- [PrestoConnector.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoConnector.java#L64-L78)
- [BaseJdbcDataSourceInfo.java](file://datavines-common/src/main/java/io/datavines/common/datasource/jdbc/BaseJdbcDataSourceInfo.java#L184-L194)
- [ConfigConstants.java](file://datavines-common/src/main/java/io/datavines/common/ConfigConstants.java#L177-L181)

## SSL/TLS加密连接配置

Presto连接器支持SSL/TLS加密连接，通过在连接属性中配置相应的SSL参数实现。虽然具体实现未在Presto专用类中体现，但系统通过通用的JDBC连接框架支持SSL配置。

### SSL配置方式

SSL配置通过`PROPERTIES`参数传递，可以包含以下常见的SSL相关属性：
- `SSL=true`：启用SSL加密
- `SSLTrustStorePath`：信任库路径
- `SSLTrustStorePassword`：信任库密码
- `SSLEngineHostVerification=false`：禁用主机名验证（测试环境）

这些属性会直接附加到JDBC URL的查询字符串中，由Presto JDBC驱动程序处理。

### 证书管理

系统本身不直接管理SSL证书，而是依赖JVM的信任库机制。用户需要：
1. 将Presto服务器的CA证书导入到JVM的信任库中
2. 或者通过连接属性指定自定义的信任库路径和密码
3. 确保运行环境的`java.security.krb5.conf`等安全配置正确

**本节来源**  
- [PrestoParameterConverter.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoParameterConverter.java#L44-L47)
- [PrestoDataSourceInfo.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceInfo.java#L43)

## 高级配置选项

Presto连接器支持多种高级配置选项，包括会话属性、查询超时、资源组配置等，这些都通过通用的属性机制进行管理。

### 会话属性配置

会话属性可以通过`PROPERTIES`参数进行设置，影响查询执行的会话级别行为。常见的会话属性包括：
- `query_max_run_time`：查询最大运行时间
- `query_priority`：查询优先级
- `resource_group`：资源组名称
- `timezone`：会话时区

### 查询超时设置

查询超时可以通过以下方式配置：
- `query_max_execution_time`：查询最大执行时间
- `client_timeout`：客户端超时时间
- `query_max_cpu_time`：查询最大CPU时间

这些超时设置有助于防止长时间运行的查询占用过多资源。

### 资源组配置

资源组（Resource Group）用于控制查询的资源分配和并发度。可以通过以下属性进行配置：
- `resource_group`：指定查询所属的资源组
- `query_type`：查询类型（如SELECT、INSERT等）
- `source`：查询来源标识

**本节来源**  
- [PrestoParameterConverter.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoParameterConverter.java#L44-L47)
- [PrestoDataSourceInfo.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceInfo.java#L43)

## 性能调优建议

为了优化Presto连接的性能，建议采取以下措施：

### 连接池配置

系统使用`commons-dbcp2`作为连接池实现，建议配置以下参数：
- `maxTotal`：最大连接数，根据并发需求设置
- `maxIdle`：最大空闲连接数
- `minIdle`：最小空闲连接数
- `maxWaitMillis`：获取连接的最大等待时间

### 批量操作优化

对于大量数据操作，建议：
- 使用批量执行模式减少网络往返
- 合理设置`fetchSize`以优化结果集获取
- 利用Presto的并行查询能力

### 查询优化

- 避免SELECT *，只选择需要的列
- 使用适当的过滤条件减少数据扫描
- 利用Presto的分区和分桶特性
- 对频繁查询的表使用合适的索引策略

**本节来源**  
- [pom.xml](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/pom.xml#L50-L53)
- [PrestoDataSourceClient.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceClient.java)

## 连接测试方法

系统提供了内置的连接测试功能，通过`PrestoConnector`类的`testConnect`方法实现。

### 测试流程

连接测试的执行流程如下：
1. 解析数据源参数为Map结构
2. 创建`PrestoDataSourceInfo`实例
3. 加载JDBC驱动类
4. 构建包含认证信息的`Properties`对象
5. 从JDBC URL中分离基础URL和查询参数
6. 使用`DriverManager.getConnection()`建立连接
7. 执行`SHOW SCHEMAS`命令验证连接有效性
8. 关闭连接并返回测试结果

### 测试结果

测试结果通过`ConnectorResponse`对象返回，包含：
- `status`：测试状态（SUCCESS/FAILURE）
- `result`：布尔值，表示连接是否成功
- 错误信息记录在日志中

测试方法确保不仅能够建立连接，还能成功执行基本的元数据查询，验证了连接的完整可用性。

**本节来源**  
- [PrestoConnector.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoConnector.java#L59-L97)
- [PrestoDataSourceInfo.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceInfo.java#L49)
- [PrestoDataSourceClient.java](file://datavines-connector/datavines-connector-plugins/datavines-connector-presto/src/main/java/io/datavines/connector/plugin/PrestoDataSourceClient.java#L58-L64)