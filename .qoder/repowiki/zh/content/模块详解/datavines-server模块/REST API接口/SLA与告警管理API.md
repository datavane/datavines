# SLA与告警管理API

<cite>
**本文档引用的文件**   
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java)
- [SlaCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaCreate.java)
- [SlaNotificationCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaNotificationCreate.java)
- [SlaSenderCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaSenderCreate.java)
- [SlaNotificationService.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/SlaNotificationService.java)
- [SlaSenderService.java](file://datavines-server/src/main/java/io/datavines/server/repository/service/SlaSenderService.java)
- [SlaNotificationVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/SlaNotificationVO.java)
- [SlaSenderVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/SlaSenderVO.java)
</cite>

## 目录
1. [简介](#简介)
2. [SLA管理接口](#sla管理接口)
3. [SLA告警通知接口](#sla告警通知接口)
4. [通知渠道配置](#通知渠道配置)
5. [SLA规则创建流程示例](#sla规则创建流程示例)
6. [告警状态与通知历史查询](#告警状态与通知历史查询)

## 简介
SLA与告警管理API提供了创建、更新和管理SLA规则及告警通知的完整功能。系统通过SlaController和SlaNotificationController两个主要控制器提供RESTful接口，支持将SLA规则与质量检查任务关联，并配置多种通知渠道（如钉钉、邮件等）进行告警通知。

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L43-L48)

## SLA管理接口

### SLA规则管理
SlaController提供了完整的SLA规则管理接口，包括创建、更新、查询和删除操作。

#### 创建SLA规则
通过POST请求创建新的SLA规则，请求体包含SlaCreate对象：

```json
{
  "name": "string",
  "description": "string",
  "workspaceId": 0
}
```

**参数说明：**
- **name**: SLA规则名称，不能为空
- **description**: SLA规则描述
- **workspaceId**: 工作空间ID，不能为空

#### 更新SLA规则
通过PUT请求更新现有SLA规则，请求体包含SlaUpdate对象（继承自SlaCreate并添加ID字段）。

#### 查询SLA规则
支持多种查询方式：
- 分页查询：`GET /api/v1/sla/page?workspaceId=1&pageNumber=1&pageSize=10`
- 根据SLA ID查询：`GET /api/v1/sla/{slaId}`
- 根据任务ID查询：`GET /api/v1/sla/job/{jobId}`

#### 删除SLA规则
通过DELETE请求删除指定ID的SLA规则：`DELETE /api/v1/sla/{id}`

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L122-L150)
- [SlaCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaCreate.java#L24-L34)

## SLA告警通知接口

### 告警通知管理
SlaNotificationController（集成在SlaController中）提供了告警通知的管理功能。

#### 创建告警通知
通过POST请求创建告警通知配置：

```json
{
  "type": "string",
  "workspaceId": 0,
  "slaId": 0,
  "senderId": 0,
  "config": "string"
}
```

**参数说明：**
- **type**: 通知类型（如"email"、"dingtalk"等）
- **workspaceId**: 工作空间ID
- **slaId**: 关联的SLA规则ID
- **senderId**: 通知发送器ID
- **config**: 通知配置JSON字符串

#### 更新告警通知
通过PUT请求更新告警通知配置，请求体为SlaNotificationUpdate对象（包含ID字段）。

#### 查询告警通知
支持分页查询告警通知配置：`GET /api/v1/sla/notification/page?workspaceId=1&slaId=1&pageNumber=1&pageSize=10`

#### 删除告警通知
通过DELETE请求删除指定ID的告警通知：`DELETE /api/v1/sla/notification/{id}`

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L205-L231)
- [SlaNotificationCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaNotificationCreate.java#L26-L37)

## 通知渠道配置

### 通知发送器管理
系统支持多种通知渠道，通过SlaSender管理通知发送器配置。

#### 创建通知发送器
通过POST请求创建通知发送器：

```json
{
  "workspaceId": 0,
  "type": "string",
  "name": "string",
  "config": "string"
}
```

**参数说明：**
- **type**: 发送器类型（如"email"、"dingtalk"等）
- **name**: 发送器名称
- **config**: 发送器配置JSON字符串

#### 支持的插件类型
通过`GET /api/v1/sla/plugin/support`接口获取系统支持的所有通知插件类型。

#### 获取配置模板
通过`GET /api/v1/sla/sender/config/{type}`接口获取指定类型发送器的配置参数模板。

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L170-L203)
- [SlaSenderCreate.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/bo/sla/SlaSenderCreate.java#L25-L34)

## SLA规则创建流程示例

### 完整创建流程
以下是创建SLA规则并与质量检查任务关联的完整流程：

1. **创建通知发送器**
   ```http
   POST /api/v1/sla/sender
   Content-Type: application/json
   
   {
     "workspaceId": 1,
     "type": "email",
     "name": "生产环境邮件通知",
     "config": "{\"smtpHost\":\"smtp.example.com\",\"smtpPort\":587,\"username\":\"user\",\"password\":\"pass\",\"from\":\"alert@example.com\"}"
   }
   ```

2. **创建SLA规则**
   ```http
   POST /api/v1/sla
   Content-Type: application/json
   
   {
     "name": "核心表数据质量SLA",
     "description": "确保核心业务表的数据质量达标",
     "workspaceId": 1
   }
   ```

3. **创建告警通知配置**
   ```http
   POST /api/v1/sla/notification
   Content-Type: application/json
   
   {
     "type": "email",
     "workspaceId": 1,
     "slaId": 1,
     "senderId": 1,
     "config": "{\"to\":[\"dba@company.com\",\"dev@company.com\"],\"cc\":[\"manager@company.com\"]}"
   }
   ```

4. **将质量检查任务关联到SLA**
   ```http
   POST /api/v1/sla/job/createOrUpdate
   Content-Type: application/json
   
   {
     "slaId": 1,
     "jobIds": [1001, 1002, 1003]
   }
   ```

5. **测试告警配置**
   ```http
   GET /api/v1/sla/test/1
   ```
   此接口会发送测试告警，验证通知配置是否正确。

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L81-L91)
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L93-L101)

## 告警状态与通知历史查询

### 告警状态查询
系统提供了多种方式查询SLA相关的告警状态：

#### 查询SLA任务列表
获取与特定SLA规则关联的所有质量检查任务：

```http
GET /api/v1/sla/job/page?slaId=1&pageNumber=1&pageSize=10
```

或获取所有关联任务的列表：
```http
GET /api/v1/sla/job/list?slaId=1
```

#### 查询SLA规则列表
分页查询工作空间内的所有SLA规则：
```http
GET /api/v1/sla/page?workspaceId=1&pageNumber=1&pageSize=10
```

### 通知历史记录
虽然当前API未直接提供通知历史查询接口，但可以通过以下方式获取相关信息：

1. **查看告警通知配置历史**
   ```http
   GET /api/v1/sla/notification/page?workspaceId=1&slaId=1&pageNumber=1&pageSize=10
   ```
   此接口返回当前有效的告警通知配置，包含更新时间和更新人信息。

2. **通过日志系统获取通知历史**
   系统在发送告警时会记录日志，可通过日志系统查询历史通知记录。

3. **监控通知发送器状态**
   ```http
   GET /api/v1/sla/sender/page?workspaceId=1&pageNumber=1&pageSize=10
   ```
   查看通知发送器的配置和状态。

**Section sources**
- [SlaController.java](file://datavines-server/src/main/java/io/datavines/server/api/controller/SlaController.java#L66-L78)
- [SlaNotificationVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/SlaNotificationVO.java#L26-L50)
- [SlaSenderVO.java](file://datavines-server/src/main/java/io/datavines/server/api/dto/vo/SlaSenderVO.java#L24-L45)