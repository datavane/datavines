# MySQL配置

<cite>
**本文档引用的文件**
- [MysqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-mysql\src\main\java\io\datavines\connector\plugin\MysqlParameterConverter.java)
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)
- [ConfigConstants.java](file://datavines-common\src\main\java\io\datavines\common\ConfigConstants.java)
- [JdbcConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConfigBuilder.java)
- [JdbcParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcParameterConverter.java)
- [ConnectionInfo.java](file://datavines-common\src\main\java\io\datavines\common\entity\ConnectionInfo.java)
- [MysqlMutex.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlMutex.java)
- [MysqlServerStateManager.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlServerStateManager.java)
- [MysqlRegistry.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlRegistry.java)
</cite>

## 目录
1. [简介](#简介)
2. [连接字符串格式](#连接字符串格式)
3. [连接参数配置](#连接参数配置)
4. [SSL配置](#ssl配置)
5. [连接池与性能调优](#连接池与性能调优)
6. [MySQL特有配置](#mysql特有配置)
7. [连接测试](#连接测试)
8. [故障排查](#故障排查)

## 简介
本文档详细说明了DataVines平台中MySQL连接器的配置方法。文档涵盖了JDBC连接字符串格式、连接参数配置、SSL设置、连接池参数、性能调优建议以及连接测试方法。通过分析DataVines代码库中的相关实现，提供了MySQL数据库配置的完整指南。

**本文档引用的文件**
- [MysqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-mysql\src\main\java\io\datavines\connector\plugin\MysqlParameterConverter.java)
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)

## 连接字符串格式
MySQL连接器使用标准的JDBC连接字符串格式，其基本结构如下：

```
jdbc:mysql://host:port/database
```

其中：
- `host`：MySQL服务器的主机名或IP地址
- `port`：MySQL服务器的端口号（默认为3306）
- `database`：要连接的数据库名称

在DataVines系统中，连接字符串的构建是通过`MysqlParameterConverter`类实现的。该类根据提供的参数动态生成JDBC URL：

```java
String url = String.format("jdbc:mysql://%s:%s/%s",
        parameter.get(HOST),
        parameter.get(PORT),
        parameter.get(DATABASE));
```

如果需要添加额外的连接参数，可以通过`properties`字段指定，这些参数将以查询字符串的形式附加到URL后面：

```java
String properties = (String)parameter.get(PROPERTIES);
if (StringUtils.isNotEmpty(properties)) {
    url += "?" + properties;
}
```

例如，一个完整的连接字符串可能如下所示：
```
jdbc:mysql://localhost:3306/mydatabase?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true
```

**本文档引用的文件**
- [MysqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-mysql\src\main\java\io\datavines\connector\plugin\MysqlParameterConverter.java)
- [ConfigConstants.java](file://datavines-common\src\main\java\io\datavines\common\ConfigConstants.java)

## 连接参数配置
DataVines平台支持通过参数化配置来设置MySQL连接的各种选项。这些参数主要通过`JdbcConfigBuilder`和`JdbcParameterConverter`类进行处理。

### 基本连接参数
以下是MySQL连接器支持的基本参数：

| 参数 | 说明 | 是否必需 |
|------|------|---------|
| host | 数据库服务器地址 | 是 |
| port | 数据库服务器端口 | 是 |
| database | 要连接的数据库名称 | 是 |
| user | 数据库用户名 | 是 |
| password | 数据库密码 | 否 |
| properties | 额外的连接属性 | 否 |

这些参数在`JdbcConfigBuilder`类中定义，通过`getHostInput`、`getPortInput`、`getDatabaseInput`等方法创建相应的输入字段。

### 连接属性
通过`properties`参数可以传递额外的JDBC连接属性，这些属性以`key=value&key1=value1`的格式指定。常见的MySQL连接属性包括：

- `useSSL`：是否启用SSL连接
- `serverTimezone`：服务器时区设置
- `allowPublicKeyRetrieval`：是否允许公钥检索
- `connectTimeout`：连接超时时间（毫秒）
- `socketTimeout`：套接字超时时间（毫秒）
- `autoReconnect`：是否自动重连
- `maxReconnects`：最大重连次数
- `initialTimeout`：初始重连延迟（秒）

在DataVines中，这些属性可以通过UI界面或API调用进行配置，最终会被附加到JDBC URL的查询字符串部分。

**本文档引用的文件**
- [JdbcConfigBuilder.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcConfigBuilder.java)
- [JdbcParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-jdbc\src\main\java\io\datavines\connector\plugin\JdbcParameterConverter.java)
- [ConfigConstants.java](file://datavines-common\src\main\java\io\datavines\common\ConfigConstants.java)

## SSL配置
DataVines平台支持MySQL的SSL连接配置，以确保数据库通信的安全性。SSL配置主要通过连接属性来实现。

### SSL连接参数
要启用SSL连接，需要在连接属性中设置相应的参数：

```properties
useSSL=true&requireSSL=true&verifyServerCertificate=true
```

- `useSSL=true`：启用SSL连接
- `requireSSL=true`：要求使用SSL连接
- `verifyServerCertificate=true`：验证服务器证书

### 自定义信任库
如果需要使用自定义的信任库，可以添加以下参数：

```properties
trustCertificateKeyStoreUrl=file:/path/to/truststore&trustCertificateKeyStorePassword=password
```

在DataVines的代码实现中，SSL配置是通过`properties`参数传递的，系统会将其直接附加到JDBC连接字符串中。对于需要更复杂SSL配置的场景，可以通过扩展`MysqlParameterConverter`类来实现自定义的SSL配置逻辑。

**本文档引用的文件**
- [MysqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-mysql\src\main\java\io\datavines\connector\plugin\MysqlParameterConverter.java)
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)

## 连接池与性能调优
DataVines平台通过连接池管理来优化MySQL连接的性能和资源利用率。

### 连接复用
系统通过`JdbcExecutorClientManager`类实现连接的复用和管理：

```java
private final ConcurrentHashMap<String, JdbcExecutorClient> clientMap = new ConcurrentHashMap<>();
```

该类使用单例模式，通过ConcurrentHashMap存储和管理JDBC连接客户端，确保连接的高效复用。

### 批量操作优化
对于批量数据操作，建议使用以下配置来优化性能：

- `rewriteBatchedStatements=true`：启用批处理语句重写
- `useServerPrepStmts=true`：使用服务器端预处理语句
- `cachePrepStmts=true`：缓存预处理语句
- `prepStmtCacheSize=250`：预处理语句缓存大小
- `prepStmtCacheSqlLimit=2048`：预处理语句SQL长度限制

### 查询超时设置
为了防止长时间运行的查询影响系统性能，建议设置查询超时：

```properties
socketTimeout=30000&connectTimeout=10000
```

- `socketTimeout`：设置套接字读取超时时间
- `connectTimeout`：设置连接超时时间

这些超时设置可以有效防止查询阻塞，提高系统的稳定性和响应性。

**本文档引用的文件**
- [JdbcExecutorClientManager.java](file://datavines-common\src\main\java\io\datavines\common\datasource\jdbc\JdbcExecutorClientManager.java)
- [MysqlParameterConverter.java](file://datavines-connector\datavines-connector-plugins\datavines-connector-mysql\src\main\java\io\datavines\connector\plugin\MysqlParameterConverter.java)

## MySQL特有配置
DataVines平台针对MySQL数据库提供了一些特有的配置选项和功能。

### 故障转移配置
DataVines使用MySQL作为注册中心时，实现了基于MySQL的故障转移机制。通过`MysqlMutex`和`MysqlServerStateManager`类实现分布式锁和服务器状态管理：

```java
public class MysqlMutex {
    private Connection connection;
    private final Properties properties;
    private final ServerInfo serverInfo;
    private final ConcurrentHashMap<String, RegistryLock> lockHoldMap;
}
```

这种机制确保了在多节点部署时的高可用性和故障转移能力。

### 读写分离
虽然DataVines本身没有直接实现读写分离，但可以通过配置多个数据源来实现读写分离：

- 主数据源：用于写操作
- 从数据源：用于读操作

通过在应用程序层面路由读写请求到不同的数据源，可以实现基本的读写分离功能。

### 批量插入优化
对于批量插入操作，建议使用以下优化配置：

```properties
rewriteBatchedStatements=true&useServerPrepStmts=true&cachePrepStmts=true
```

这些配置可以显著提高批量插入的性能，特别是在处理大量数据时。

**本文档引用的文件**
- [MysqlMutex.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlMutex.java)
- [MysqlServerStateManager.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlServerStateManager.java)
- [MysqlRegistry.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlRegistry.java)

## 连接测试
DataVines平台提供了多种方式来测试MySQL连接的连通性和性能。

### 连通性测试
系统通过`JdbcUrlParser`类解析和验证连接信息：

```java
public static ConnectionInfo getConnectionInfo(String jdbcUrl, String username, String password) {
    // 解析JDBC URL并返回连接信息
}
```

该方法可以验证连接字符串的正确性，并提取出主机、端口、数据库等基本信息。

### 性能测试
为了测试连接性能，建议使用以下方法：

1. **连接建立时间**：测量从发起连接到成功建立连接的时间
2. **查询响应时间**：执行简单的查询语句（如`SELECT 1`）并测量响应时间
3. **并发连接测试**：测试系统在高并发情况下的连接性能

在DataVines中，这些测试可以通过调用相应的API接口或使用内置的测试工具来完成。

### 健康检查
系统定期执行健康检查来确保MySQL连接的可用性：

```java
public boolean acquire(String lockKey, long timeout){
    return registry.acquire(key, timeout);
}
```

通过尝试获取分布式锁来验证连接的活跃状态，确保系统能够及时发现和处理连接问题。

**本文档引用的文件**
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)
- [ConnectionInfo.java](file://datavines-common\src\main\java\io\datavines\common\entity\ConnectionInfo.java)

## 故障排查
当MySQL连接出现问题时，可以参考以下排查步骤：

### 常见问题及解决方案
1. **连接超时**
   - 检查网络连接是否正常
   - 验证主机名和端口是否正确
   - 检查防火墙设置

2. **认证失败**
   - 确认用户名和密码是否正确
   - 检查用户是否有连接数据库的权限
   - 验证数据库是否允许远程连接

3. **SSL连接问题**
   - 确认服务器证书是否有效
   - 检查SSL配置参数是否正确
   - 验证信任库设置

### 日志分析
查看系统日志中的错误信息，重点关注以下内容：
- 连接建立失败的详细错误信息
- SSL握手失败的原因
- 连接池耗尽的警告

通过分析日志可以快速定位问题根源，并采取相应的解决措施。

**本文档引用的文件**
- [JdbcUrlParser.java](file://datavines-common\src\main\java\io\datavines\common\utils\JdbcUrlParser.java)
- [MysqlMutex.java](file://datavines-registry\datavines-registry-plugins\datavines-registry-mysql\src\main\java\io\datavines\registry\plugin\MysqlMutex.java)