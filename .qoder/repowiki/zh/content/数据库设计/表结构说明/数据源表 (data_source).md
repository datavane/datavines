# 数据源表 (data_source)

<cite>
**本文档引用的文件**
- [datavines-mysql.sql](file://scripts/sql/datavines-mysql.sql#L417-L432)
- [DataSource.java](file://datavines-server/src/main/java/io/datavines/server/repository/entity/DataSource.java#L1-L70)
- [DataSourceCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/datasource/DataSourceCreate.java#L1-L40)
- [MysqlDataSourceInfo.java](file://datavines-connector/ datavines-connector-plugins/datavines-connector-mysql/src/main/java/io/datavines/connector/plugin/MysqlDataSourceInfo.java#L1-L50)
</cite>

## 目录
1. [字段列表](#字段列表)
2. [业务含义说明](#业务含义说明)
3. [表设计考虑](#表设计考虑)
4. [索引与约束](#索引与约束)
5. [系统角色](#系统角色)

## 字段列表

数据源表（`dv_datasource`）包含以下字段：

| 字段名 | 数据类型 | 是否可空 | 默认值 | 说明 |
| :--- | :--- | :--- | :--- | :--- |
| `id` | bigint(20) | 否 | 无 | 主键，自增ID |
| `uuid` | varchar(64) | 否 | 无 | 数据源UUID，唯一标识 |
| `name` | varchar(255) | 否 | 无 | 数据源名称 |
| `type` | varchar(255) | 否 | 无 | 数据源类型，如mysql、postgresql等 |
| `param` | text | 否 | 无 | 数据源参数，以JSON格式存储连接信息 |
| `param_code` | text | 是 | NULL | 数据源参数的MD5值，用于快速比较参数是否变更 |
| `workspace_id` | bigint(20) | 否 | 无 | 所属工作空间ID |
| `create_by` | bigint(20) | 否 | 无 | 创建用户ID |
| `create_time` | datetime | 否 | CURRENT_TIMESTAMP | 创建时间 |
| `update_by` | bigint(20) | 否 | 无 | 更新用户ID |
| `update_time` | datetime | 否 | CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP | 更新时间 |

**字段来源**
- [datavines-mysql.sql](file://scripts/sql/datavines-mysql.sql#L417-L432)
- [DataSource.java](file://datavines-server/src/main/java/io/datavines/server/repository/entity/DataSource.java#L1-L70)

## 业务含义说明

- **`name` 字段**：表示数据源的名称，是用户在系统中识别该数据源的主要标识。名称在工作空间内必须唯一。
- **`type` 字段**：表示数据源的类型，决定了系统使用哪个连接器（Connector）来访问该数据源。根据代码中的 `MysqlDataSourceInfo.java` 文件，支持的类型包括 `mysql`、`postgresql`、`oracle` 等。
- **`param` 字段**：以JSON格式存储连接该数据源所需的所有参数。这些参数通常包括主机地址（host）、端口（port）、用户名（username）、密码（password）等。密码等敏感信息在此JSON中存储时会被加密。
- **`param_code` 字段**：存储`param`字段内容的MD5哈希值。当用户修改数据源配置时，系统可以通过比较新的参数MD5值与数据库中存储的`param_code`来判断连接参数是否真正发生了变化，从而避免不必要的元数据刷新等操作。
- **`workspace_id` 字段**：表示该数据源属于哪个工作空间，用于实现多租户和资源隔离。

**字段来源**
- [DataSourceCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/datasource/DataSourceCreate.java#L1-L40)
- [MysqlDataSourceInfo.java](file://datavines-connector/ datavines-connector-plugins/datavines-connector-mysql/src/main/java/io/datavines/connector/plugin/MysqlDataSourceInfo.java#L1-L50)

## 表设计考虑

- **使用JSON格式存储连接参数**：`param`字段使用TEXT类型存储JSON字符串，这种设计提供了极大的灵活性。不同的数据库类型（如MySQL, MongoDB, Hive）具有完全不同的连接参数，使用JSON可以轻松容纳这些差异，而无需为每种数据库类型设计固定的列。这使得系统可以方便地扩展对新数据源类型的支持。
- **使用加密字段存储敏感信息**：虽然`param`字段本身是明文存储的JSON，但根据系统设计惯例和安全要求，其中的密码（password）等敏感信息在存入数据库前会被加密。系统在使用时会先解密再建立连接，从而保护了用户的敏感凭证。
- **UUID作为唯一标识**：除了自增的`id`，还使用了`uuid`字段作为数据源的全局唯一标识。这有利于在分布式系统或数据同步场景下进行数据识别和关联。

## 索引与约束

- **主键约束 (Primary Key)**：`id` 字段是表的主键，确保了每条记录的唯一性。
- **唯一索引 (Unique Key)**：`datasource_un` 索引作用于 `name` 字段，确保在同一个工作空间内，数据源名称不能重复，防止用户创建同名的数据源造成混淆。

**索引来源**
- [datavines-mysql.sql](file://scripts/sql/datavines-mysql.sql#L431-L432)

## 系统角色

`dv_datasource` 表在系统中扮演着**所有数据质量检查任务的数据来源配置中心**的角色。它是整个数据质量平台的基础。其他核心功能都依赖于此表：
- **数据质量检查任务 (Job)**：在创建数据质量检查任务时，必须从 `dv_datasource` 表中选择一个已配置的数据源作为检查目标。
- **元数据抓取**：系统通过读取此表中的连接信息，定期连接到各个数据源，抓取数据库、表、列等元数据信息，并存储在 `dv_catalog_entity_instance` 等表中。
- **连接测试**：前端UI提供的“测试连接”功能，其后端逻辑就是根据此表中存储的 `param` 信息，尝试建立数据库连接，并返回结果。