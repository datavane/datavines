# 数据源API

<cite>
**本文档引用的文件**
- [DataSourceController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/DataSourceController.java)
- [DataSourceService.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/DataSourceService.java)
- [DataSourceServiceImpl.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/impl/DataSourceServiceImpl.java)
- [DataSourceCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/datasource/DataSourceCreate.java)
- [DataSourceUpdate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/datasource/DataSourceUpdate.java)
- [TestConnectionRequestParam.java](file://datavines-common/src/main/java/io/datavines/common/param/TestConnectionRequestParam.java)
- [ConnectorRequestParam.java](file://datavines-common/src/main/java/io/datavines/common/param/ConnectorRequestParam.java)
- [ConnectorResponse.java](file://datavines-common/src/main/java/io/datavines/common/param/ConnectorResponse.java)
- [ResultMap.java](file://datavines-core/src/main/java/io/datavines/core/entity/ResultMap.java)
- [DataSource.java](file://datavines-server/src/main/java/io/datavines/server/repository/entity/DataSource.java)
- [DataSourceVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/DataSourceVO.java)
- [ConnectionInfo.java](file://datavines-common/src/main/java/io/datavines/common/entity/ConnectionInfo.java)
- [JdbcConfigBuilder.java](file://datavines-connector/connector-plugins/connector-jdbc/src/main/java/io/datavines/connector/plugin/JdbcConfigBuilder.java)
- [OracleParameterConverter.java](file://datavines-connector/connector-plugins/connector-oracle/src/main/java/io/datavines/connector/plugin/OracleParameterConverter.java)
- [DataVinesServerException.java](file://datavines-core/src/main/java/io/datavines/core/exception/DataVinesServerException.java)
</cite>

## 更新摘要
**变更内容**
- 更新了数据源连接测试API的响应格式，从简单布尔值改为结构化的ResultMap响应
- 新增了ResultMap通用响应封装类的详细说明
- 更新了连接测试API的请求和响应示例
- 完善了错误处理机制的说明

## 目录
1. [简介](#简介)
2. [API端点](#api端点)
3. [数据源配置JSON结构](#数据源配置json结构)
4. [请求和响应示例](#请求和响应示例)
5. [连接测试API](#连接测试api)
6. [错误处理机制](#错误处理机制)
7. [最佳实践和安全建议](#最佳实践和安全建议)

## 简介

数据源API提供了管理数据源的完整功能，包括创建、更新、删除和查询数据源。该API支持多种数据库类型，如MySQL、PostgreSQL、Oracle等，并提供了测试连接、执行脚本和获取元数据的功能。API基于RESTful设计原则，使用JSON格式进行数据交换。

**数据源API的主要功能包括：**
- 创建新的数据源配置
- 更新现有数据源配置
- 删除数据源
- 查询数据源列表和分页信息
- 测试数据源连接
- 获取数据库、表和列的元数据
- 执行SQL脚本
- 获取特定类型数据源的配置JSON模板

**Section sources**
- [DataSourceController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/DataSourceController.java#L44-L167)

## API端点

### 创建数据源
创建一个新的数据源配置。

**HTTP方法**: POST  
**URL路径**: `/api/datasource`  
**内容类型**: `application/json`

**请求体结构**:
- `workspaceId`: 工作空间ID (long, 必填)
- `name`: 数据源名称 (string, 必填)
- `type`: 数据源类型 (string, 必填)
- `param`: 数据源连接参数 (string, 必填, JSON格式)

**响应格式**: 返回创建的数据源ID

### 更新数据源
更新现有数据源配置。

**HTTP方法**: PUT  
**URL路径**: `/api/datasource`  
**内容类型**: `application/json`

**请求体结构**:
- `id`: 数据源ID (long, 必填)
- `workspaceId`: 工作空间ID (long, 必填)
- `name`: 数据源名称 (string, 必填)
- `type`: 数据源类型 (string, 必填)
- `param`: 数据源连接参数 (string, 必填, JSON格式)

**响应格式**: 返回更新结果状态

### 删除数据源
删除指定ID的数据源。

**HTTP方法**: DELETE  
**URL路径**: `/api/datasource/{id}`

**响应格式**: 返回删除结果状态

### 查询数据源分页列表
获取数据源的分页列表。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/page`

**查询参数**:
- `searchVal`: 搜索关键字 (可选)
- `workSpaceId`: 工作空间ID (必填)
- `pageNumber`: 页码 (必填)
- `pageSize`: 每页大小 (必填)

**响应格式**: 返回分页的数据源列表

### 获取数据库列表
获取指定数据源的数据库列表。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/{id}/databases`

**响应格式**: 返回数据库列表

### 获取表列表
获取指定数据源和数据库的表列表。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/{id}/{database}/tables`

**响应格式**: 返回表列表

### 获取列列表
获取指定数据源、数据库和表的列列表。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/{id}/{database}/{table}/columns`

**响应格式**: 返回列列表

### 执行SQL脚本
在指定数据源上执行SQL脚本。

**HTTP方法**: POST  
**URL路径**: `/api/datasource/execute`  
**内容类型**: `application/json`

**请求体结构**:
- `dataSourceId`: 数据源ID
- `script`: 要执行的SQL脚本

**响应格式**: 返回执行结果

### 获取配置JSON模板
获取指定类型数据源的配置JSON模板。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/config/{type}`

**响应格式**: 返回配置JSON模板

### 获取连接器类型列表
获取所有可用的连接器类型。

**HTTP方法**: GET  
**URL路径**: `/api/datasource/type/list`

**响应格式**: 返回连接器类型列表

**Section sources**
- [DataSourceController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/DataSourceController.java#L56-L167)

## 数据源配置JSON结构

数据源配置以JSON格式存储，包含连接特定数据库所需的所有参数。不同数据库类型的配置结构有所不同，但都包含基本的连接信息。

### 通用配置参数
所有数据源类型都支持以下通用参数：
- `type`: 数据源类型标识
- `host`: 数据库主机地址
- `port`: 数据库端口
- `user`: 用户名
- `password`: 密码
- `database`: 数据库名称
- `schema`: 模式名称
- `properties`: 连接属性（以key=value&key2=value2格式）

### MySQL配置
```json
{
  "type": "mysql",
  "host": "localhost",
  "port": "3306",
  "user": "root",
  "password": "password",
  "database": "test",
  "properties": "useSSL=false&serverTimezone=UTC"
}
```

### PostgreSQL配置
```json
{
  "type": "postgresql",
  "host": "localhost",
  "port": "5432",
  "user": "postgres",
  "password": "password",
  "database": "test",
  "schema": "public",
  "properties": "sslmode=disable"
}
```

### Oracle配置
Oracle数据库使用SID或服务名进行连接：

**使用SID连接：**
```json
{
  "type": "oracle",
  "host": "localhost",
  "port": "1521",
  "user": "system",
  "password": "password",
  "sid": "ORCL",
  "properties": "oracle.net.CONNECT_TIMEOUT=10000"
}
```

**使用服务名连接：**
```json
{
  "type": "oracle",
  "host": "localhost",
  "port": "1521",
  "user": "system",
  "password": "password",
  "serviceName": "ORCLPDB1",
  "properties": "oracle.net.CONNECT_TIMEOUT=10000"
}
```

### SQL Server配置
```json
{
  "type": "sqlserver",
  "host": "localhost",
  "port": "1433",
  "user": "sa",
  "password": "password",
  "database": "test",
  "properties": "encrypt=false;trustServerCertificate=true"
}
```

### JDBC通用配置
对于支持JDBC的数据库，可以使用通用JDBC配置：
```json
{
  "type": "jdbc",
  "url": "jdbc:your_database_url",
  "driverName": "com.your.driver.Class",
  "user": "username",
  "password": "password",
  "properties": "property1=value1&property2=value2"
}
```

**Section sources**
- [ConnectionInfo.java](file://datavines-common/src/main/java/io/datavines/common/entity/ConnectionInfo.java#L32-L51)
- [JdbcConfigBuilder.java](file://datavines-connector/connector-plugins/connector-jdbc/src/main/java/io/datavines/connector/plugin/JdbcConfigBuilder.java#L36-L72)
- [OracleParameterConverter.java](file://datavines-connector/connector-plugins/connector-oracle/src/main/java/io/datavines/connector/plugin/OracleParameterConverter.java#L25-L44)

## 请求和响应示例

### 创建数据源请求示例
```json
{
  "workspaceId": 1,
  "name": "MySQL Production",
  "type": "mysql",
  "param": "{\"host\":\"192.168.1.100\",\"port\":\"3306\",\"user\":\"appuser\",\"password\":\"app123\",\"database\":\"production\",\"properties\":\"useSSL=false\"}"
}
```

### 创建数据源成功响应示例
```json
{
  "success": true,
  "msg": "操作成功",
  "data": 123
}
```

### 获取数据源分页列表请求示例
```
GET /api/datasource/page?searchVal=mysql&workSpaceId=1&pageNumber=1&pageSize=10
```

### 获取数据源分页列表成功响应示例
```json
{
  "success": true,
  "msg": "操作成功",
  "data": {
    "records": [
      {
        "id": 123,
        "uuid": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
        "name": "MySQL Production",
        "type": "mysql",
        "updater": "admin",
        "updateTime": "2023-05-15 10:30:00"
      }
    ],
    "total": 1,
    "size": 10,
    "current": 1,
    "pages": 1
  }
}
```

### 获取数据库列表响应示例
```json
{
  "success": true,
  "msg": "操作成功",
  "data": [
    {
      "uuid": "d1e2f3g4-h5i6-7890-j1k2-l3m4n5o6p7q8",
      "name": "production",
      "type": "database",
      "parentId": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
      "metadata": {}
    },
    {
      "uuid": "r9s8t7u6-v5w4-3210-x9y8-z7a6b5c4d3e2",
      "name": "staging",
      "type": "database",
      "parentId": "a1b2c3d4-e5f6-7890-g1h2-i3j4k5l6m7n8",
      "metadata": {}
    }
  ]
}
```

### 执行SQL脚本请求示例
```json
{
  "dataSourceId": 123,
  "script": "SELECT COUNT(*) FROM users WHERE created_date > '2023-01-01'"
}
```

### 执行SQL脚本成功响应示例
```json
{
  "success": true,
  "msg": "操作成功",
  "data": {
    "columns": [
      {
        "name": "COUNT(*)",
        "type": "BIGINT"
      }
    ],
    "data": [
      [12345]
    ]
  }
}
```

**Section sources**
- [DataSourceCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/datasource/DataSourceCreate.java#L26-L39)
- [DataSourceVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/DataSourceVO.java#L25-L42)

## 连接测试API

连接测试API用于验证数据源配置是否正确，能够成功连接到目标数据库。

### API端点
**HTTP方法**: POST  
**URL路径**: `/api/datasource/test`  
**内容类型**: `application/json`

### 请求参数
请求体为JSON格式，包含以下字段：
- `type`: 数据源类型
- `dataSourceParam`: 数据源连接参数（与创建数据源时的param字段相同）

### 请求示例
```json
{
  "type": "mysql",
  "dataSourceParam": "{\"host\":\"localhost\",\"port\":\"3306\",\"user\":\"root\",\"password\":\"password\",\"database\":\"test\"}"
}
```

### 响应格式
**更新**：连接测试API现在返回结构化的ResultMap响应，包含完整的状态信息和错误详情。

成功响应：
```json
{
  "code": 200,
  "msg": "Success",
  "data": true
}
```

失败响应：
```json
{
  "code": 400,
  "msg": "无法连接到数据库: Connection refused",
  "data": false
}
```

### 实现原理
连接测试API通过以下步骤验证连接：
1. 根据`type`参数获取对应的连接器工厂
2. 使用连接器工厂创建连接器实例
3. 调用连接器的`testConnect`方法进行连接测试
4. 将连接器返回的ConnectorResponse转换为ResultMap响应格式
5. 成功时返回包含`true`的data字段，失败时返回包含`false`的data字段

连接测试会实际建立数据库连接并执行简单的查询（如`SELECT 1`）来验证连接的有效性。

### ResultMap响应格式详解
ResultMap是系统通用的响应封装类，提供标准化的响应格式：

- `code`: HTTP状态码（200表示成功，400表示失败）
- `msg`: 响应消息描述
- `data`: 实际响应数据（布尔值表示连接成功与否）

**Section sources**
- [DataSourceController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/DataSourceController.java#L59-L86)
- [TestConnectionRequestParam.java](file://datavines-common/src/main/java/io/datavines/common/param/TestConnectionRequestParam.java#L22-L25)
- [ConnectorRequestParam.java](file://datavines-common/src/main/java/io/datavines/common/param/ConnectorRequestParam.java#L22-L27)
- [ConnectorResponse.java](file://datavines-common/src/main/java/io/datavines/common/param/ConnectorResponse.java#L22-L52)
- [ResultMap.java](file://datavines-core/src/main/java/io/datavines/core/entity/ResultMap.java#L29-L128)
- [DataSourceServiceImpl.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/impl/DataSourceServiceImpl.java#L75-L78)

## 错误处理机制

数据源API实现了完善的错误处理机制，确保客户端能够获得清晰的错误信息。

### 常见错误代码
| 错误代码 | 错误消息 | 说明 |
|---------|--------|------|
| 200 | Success | 操作成功 |
| 400 | 失败 | 操作失败 |
| 401 | 未授权访问 | 用户未登录或令牌无效 |
| 403 | 禁止访问 | 用户没有权限执行该操作 |
| 404 | 资源未找到 | 请求的资源不存在 |
| 409 | 冲突 | 请求与现有资源状态冲突 |
| 500 | 服务器内部错误 | 服务器处理请求时发生未知错误 |
| 503 | 服务不可用 | 服务暂时不可用 |

### 错误响应格式
所有错误响应都遵循统一的ResultMap格式：
```json
{
  "code": 400,
  "msg": "错误描述信息",
  "data": null
}
```

### 具体错误场景
#### 创建数据源时的错误
- **参数缺失**: 如果`workspaceId`、`name`、`type`或`param`为空，返回400错误
- **工作空间不存在**: 如果指定的`workspaceId`不存在，返回404错误
- **数据源名称重复**: 如果在同一工作空间中创建同名数据源，返回409错误

#### 更新数据源时的错误
- **数据源不存在**: 如果要更新的`id`对应的数据源不存在，返回404错误
- **参数无效**: 如果更新的参数不符合要求，返回400错误

#### 删除数据源时的错误
- **数据源不存在**: 如果要删除的`id`对应的数据源不存在，返回404错误
- **数据源正在使用**: 如果数据源被作业或其他资源引用，返回409错误

#### 连接测试时的错误
- **连接失败**: 如果无法连接到数据库，返回400错误，消息中包含具体的连接错误信息
- **认证失败**: 如果用户名或密码错误，返回400错误
- **连接器响应为空**: 如果连接器返回null，返回400错误

### 异常处理实现
后端使用`DataVinesServerException`类来处理所有业务异常。该异常类继承自`DataVinesException`，并包含状态码和错误消息。

```java
public class DataVinesServerException extends DataVinesException {
    private Status status;
    
    public DataVinesServerException(Status status) {
        super(status.getMsg());
        this.status = status;
    }
    
    public DataVinesServerException(Status status, Object... statusParams) {
        super(CollectionUtils.isEmpty(Arrays.asList(statusParams)) ? 
            status.getMsg() : MessageFormat.format(status.getMsg(), statusParams));
        this.status = status;
    }
    
    public Status getStatus() {
        return status;
    }
}
```

**Section sources**
- [DataVinesServerException.java](file://datavines-core/src/main/java/io/datavines/core/exception/DataVinesServerException.java#L26-L64)
- [DataSourceServiceImpl.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/impl/DataSourceServiceImpl.java#L150-L197)

## 最佳实践和安全建议

### 安全最佳实践
1. **敏感信息加密**: 所有数据源配置中的密码等敏感信息都会使用AES加密后存储在数据库中
2. **最小权限原则**: 为数据源配置的数据库用户应仅具有执行必要操作的最小权限
3. **定期轮换凭证**: 定期更新数据库用户的密码，并相应更新数据源配置
4. **网络隔离**: 将数据库部署在受保护的网络区域，仅允许应用服务器访问

### 性能优化建议
1. **连接池配置**: 对于频繁访问的数据源，建议配置适当的连接池大小
2. **索引优化**: 确保在经常查询的字段上创建适当的索引
3. **查询优化**: 避免在生产环境中执行全表扫描等低效查询
4. **缓存策略**: 对于不经常变化的元数据，可以考虑使用缓存

### 使用建议
1. **命名规范**: 使用有意义的名称来标识数据源，便于管理和识别
2. **分类管理**: 根据环境（开发、测试、生产）或业务领域对数据源进行分类管理
3. **文档记录**: 记录每个数据源的用途、负责人和相关业务信息
4. **监控告警**: 设置数据源连接状态的监控和告警，及时发现连接问题

### 开发者建议
1. **错误处理**: 在调用API时，妥善处理各种可能的错误情况
2. **重试机制**: 对于临时性错误（如网络问题），实现适当的重试机制
3. **资源清理**: 及时清理不再使用的数据源配置，避免资源浪费
4. **版本控制**: 对重要的数据源配置变更进行记录和版本控制

**Section sources**
- [DataSourceServiceImpl.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/impl/DataSourceServiceImpl.java#L113-L116)
- [DataSourceServiceImpl.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/impl/DataSourceServiceImpl.java#L184-L189)