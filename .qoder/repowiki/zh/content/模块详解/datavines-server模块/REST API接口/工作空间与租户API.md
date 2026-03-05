# 工作空间与租户API

<cite>
**本文档引用的文件**  
- [WorkSpaceController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\WorkSpaceController.java)
- [TenantController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\TenantController.java)
- [WorkSpaceService.java](file://datavines-server\src\main\java\io\datavines\server\repository\service\WorkSpaceService.java)
- [TenantService.java](file://datavines-server\src\main\java\io\datavines\server\repository\service\TenantService.java)
- [WorkSpaceCreate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\WorkSpaceCreate.java)
- [WorkSpaceUpdate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\WorkSpaceUpdate.java)
- [InviteUserIntoWorkspace.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\InviteUserIntoWorkspace.java)
- [RemoveUserOutWorkspace.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\RemoveUserOutWorkspace.java)
- [TenantCreate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\tenant\TenantCreate.java)
- [TenantUpdate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\tenant\TenantUpdate.java)
- [WorkSpace.java](file://datavines-server\src\main\java\io\datavines\server\repository\entity\WorkSpace.java)
- [Tenant.java](file://datavines-server\src\main\java\io\datavines\server\repository\entity\Tenant.java)
- [WorkSpaceVO.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\vo\WorkSpaceVO.java)
</cite>

## 目录
1. [简介](#简介)
2. [工作空间管理API](#工作空间管理api)
3. [租户管理API](#租户管理api)
4. [权限与资源访问控制](#权限与资源访问控制)
5. [API调用示例](#api调用示例)
6. [最佳实践](#最佳实践)

## 简介
本文档详细描述了DataVines平台中工作空间（Workspace）与租户（Tenant）管理的API接口。系统采用多租户架构，通过工作空间实现用户分组和资源隔离，每个工作空间下可创建多个租户用于资源配置和管理。API提供了创建、更新、删除工作空间，邀请/移除用户，以及管理租户配额等核心功能。

**本文档不包含具体代码内容，所有实现细节均通过引用源文件进行追踪。**

## 工作空间管理API

工作空间是多租户环境下的顶级组织单元，用于隔离不同团队或项目的资源。WorkSpaceController提供了完整的工作空间生命周期管理接口。

### 创建工作空间
通过POST请求创建新的工作空间，请求体需包含工作空间名称。

**接口信息**
- **方法**: POST
- **路径**: `/api/v1/workspace`
- **请求体**: `WorkSpaceCreate` 对象
- **成功响应**: 新创建的工作空间ID

### 更新工作空间
通过PUT请求更新现有工作空间的信息。

**接口信息**
- **方法**: PUT
- **路径**: `/api/v1/workspace`
- **请求体**: `WorkSpaceUpdate` 对象（包含ID和新名称）
- **成功响应**: 布尔值，表示更新是否成功

### 删除工作空间
通过DELETE请求删除指定ID的工作空间。

**接口信息**
- **方法**: DELETE
- **路径**: `/api/v1/workspace/{id}`
- **路径参数**: `id` - 工作空间ID
- **成功响应**: 布尔值，表示删除是否成功

### 查询工作空间列表
获取当前用户所属的所有工作空间列表。

**接口信息**
- **方法**: GET
- **路径**: `/api/v1/workspace/list`
- **成功响应**: `WorkSpaceVO` 对象列表

### 用户管理
提供邀请用户加入和移除用户出工作空间的功能。

#### 邀请用户
邀请新用户加入指定工作空间。

**接口信息**
- **方法**: POST
- **路径**: `/api/v1/workspace/inviteUser`
- **请求体**: `InviteUserIntoWorkspace` 对象（包含用户名、邮箱和工作空间ID）
- **成功响应**: 新创建的用户关系ID

#### 移除用户
将用户从工作空间中移除。

**接口信息**
- **方法**: DELETE
- **路径**: `/api/v1/workspace/removeUser`
- **请求体**: `RemoveUserOutWorkspace` 对象（包含用户ID和工作空间ID）
- **成功响应**: 布尔值，表示移除是否成功

#### 查询工作空间用户
分页查询指定工作空间中的用户列表。

**接口信息**
- **方法**: GET
- **路径**: `/api/v1/workspace/userPage`
- **查询参数**: `workspaceId`, `pageNumber`, `pageSize`
- **成功响应**: 分页的用户信息列表

**接口来源**
- [WorkSpaceController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\WorkSpaceController.java#L44-L88)
- [WorkSpaceService.java](file://datavines-server\src\main\java\io\datavines\server\repository\service\WorkSpaceService.java#L32-L49)
- [WorkSpaceCreate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\WorkSpaceCreate.java#L24-L31)
- [WorkSpaceUpdate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\WorkSpaceUpdate.java#L24-L32)
- [InviteUserIntoWorkspace.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\InviteUserIntoWorkspace.java#L24-L37)
- [RemoveUserOutWorkspace.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\workspace\RemoveUserOutWorkspace.java#L23-L33)
- [WorkSpaceVO.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\vo\WorkSpaceVO.java#L25-L45)

## 租户管理API

租户是工作空间下的资源管理单元，用于实现资源配额控制和环境隔离。TenantController提供了租户的CRUD操作和查询接口。

### 创建租户
在指定工作空间下创建新的租户。

**接口信息**
- **方法**: POST
- **路径**: `/api/v1/tenant`
- **请求体**: `TenantCreate` 对象（包含工作空间ID和租户名称）
- **成功响应**: 新创建的租户ID

### 更新租户
更新现有租户的信息。

**接口信息**
- **方法**: PUT
- **路径**: `/api/v1/tenant`
- **请求体**: `TenantUpdate` 对象
- **成功响应**: 布尔值，表示更新是否成功

### 删除租户
删除指定ID的租户。

**接口信息**
- **方法**: DELETE
- **路径**: `/api/v1/tenant/{id}`
- **路径参数**: `id` - 租户ID
- **成功响应**: 布尔值，表示删除是否成功

### 查询租户列表
获取指定工作空间下的所有租户列表。

**接口信息**
- **方法**: GET
- **路径**: `/api/v1/tenant/list/{workspaceId}`
- **路径参数**: `workspaceId` - 工作空间ID
- **成功响应**: `Tenant` 对象列表

### 查询租户选项
获取指定工作空间下租户的选项列表，用于前端下拉框展示。

**接口信息**
- **方法**: GET
- **路径**: `/api/v1/tenant/listOptions/{workspaceId}`
- **路径参数**: `workspaceId` - 工作空间ID
- **成功响应**: `Item` 对象列表（包含租户名称和ID）

**接口来源**
- [TenantController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\TenantController.java#L47-L85)
- [TenantService.java](file://datavines-server\src\main\java\io\datavines\server\repository\service\TenantService.java#L28-L39)
- [TenantCreate.java](file://datavines-server\src\main\java\io\datavines\server\api\dto\bo\tenant\TenantCreate.java#L24-L33)

## 权限与资源访问控制

系统实现了基于工作空间的多层权限控制机制，确保资源的安全访问。

### 认证与授权
所有API请求都需要有效的认证令牌。系统通过`@RefreshToken`注解实现令牌刷新机制，确保长时间会话的安全性。`AuthenticationInterceptor`拦截器负责验证用户身份和权限。

### 跨工作空间访问限制
系统严格限制跨工作空间的资源访问：
- 用户只能访问其所属工作空间的资源
- 租户与工作空间强关联，无法跨空间访问
- 数据源、作业等资源均绑定到特定工作空间
- API调用时需确保操作的资源属于用户有权访问的工作空间

### 权限分配
权限通过工作空间成员关系进行分配：
- 工作空间创建者默认拥有管理员权限
- 邀请加入的用户根据角色获得相应权限
- 租户级别的资源配置影响该租户下所有作业的资源使用

**权限来源**
- [WorkSpaceController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\WorkSpaceController.java#L38)
- [TenantController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\TenantController.java#L41)
- [AuthenticationInterceptor.java](file://datavines-server\src\main\java\io\datavines\server\inteceptor\AuthenticationInterceptor.java)

## API调用示例

### 创建工作空间
```http
POST /api/v1/workspace HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "name": "数据分析团队"
}
```

### 邀请用户
```http
POST /api/v1/workspace/inviteUser HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "username": "zhangsan",
  "email": "zhangsan@company.com",
  "workspaceId": 1
}
```

### 创建租户
```http
POST /api/v1/tenant HTTP/1.1
Content-Type: application/json
Authorization: Bearer <token>

{
  "workspaceId": 1,
  "tenant": "production"
}
```

### 查询工作空间用户
```http
GET /api/v1/workspace/userPage?workspaceId=1&pageNumber=1&pageSize=10 HTTP/1.1
Authorization: Bearer <token>
```

## 最佳实践

### 工作空间设计
- 为不同团队或项目创建独立的工作空间
- 使用有意义的名称便于识别和管理
- 定期审查工作空间成员，确保权限最小化

### 租户管理
- 在每个工作空间内创建多个租户用于环境隔离（如dev、test、prod）
- 通过租户实现资源配额控制
- 遵循命名规范，便于识别租户用途

### 安全建议
- 严格控制工作空间创建权限
- 定期审计用户邀请和移除操作
- 使用强密码策略和多因素认证
- 限制敏感操作的权限范围

### 性能考虑
- 批量操作时使用分页查询避免性能问题
- 合理设置租户资源配额防止资源滥用
- 定期清理不再使用的租户和工作空间

**最佳实践来源**
- [WorkSpaceController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\WorkSpaceController.java)
- [TenantController.java](file://datavines-server\src\main\java\io\datavines\server\api\controller\TenantController.java)
- [application.yaml](file://datavines-server\src\main\resources\application.yaml)