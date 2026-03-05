# ZooKeeper实现

<cite>
**本文档引用文件**  
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [DefaultEnsembleProvider.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/DefaultEnsembleProvider.java)
- [ZooKeeperRegistry.java](file://datavines-registry/ datavines-registry-plugins/ datavines-registry-zookeeper/ src/main/java/io/ datavines/registry/plugin/ZooKeeperRegistry.java)
- [Registry.java](file://datavines-registry/ datavines-registry-api/ src/main/java/io/ datavines/registry/api/Registry.java)
- [ConnectionListener.java](file://datavines-registry/ datavines-registry-api/ src/main/java/io/ datavines/registry/api/ConnectionListener.java)
- [SubscribeListener.java](file://datavines-registry/ datavines-registry-api/ src/main/java/io/ datavines/registry/api/SubscribeListener.java)
</cite>

## 目录
1. [简介](#简介)
2. [项目结构](#项目结构)
3. [核心组件](#核心组件)
4. [架构概述](#架构概述)
5. [详细组件分析](#详细组件分析)
6. [依赖分析](#依赖分析)
7. [性能考虑](#性能考虑)
8. [故障排除指南](#故障排除指南)
9. [结论](#结论)

## 简介
本文档详细描述了DataVines中基于ZooKeeper的服务注册与发现机制。文档涵盖了ZooKeeper客户端封装、服务注册、心跳检测、故障剔除、连接状态监听等核心功能的实现原理。通过分析ZooKeeperRegistry、ZooKeeperClient等关键组件，深入解析了临时节点的使用方式、会话管理机制以及ZooKeeper配置参数的调优建议。

## 项目结构
DataVines的ZooKeeper注册中心实现主要分布在两个模块中：`datavines-common`和`datavines-registry-plugins`。`datavines-common`模块提供了ZooKeeper客户端的基础封装，而`datavines-registry-plugins`模块实现了具体的注册中心功能。

```mermaid
graph TD
subgraph "datavines-common"
ZooKeeperClient["ZooKeeperClient<br/>客户端封装"]
ZooKeeperConfig["ZooKeeperConfig<br/>配置类"]
DefaultEnsembleProvider["DefaultEnsembleProvider<br/>集群提供者"]
end
subgraph "datavines-registry-plugins"
ZooKeeperRegistry["ZooKeeperRegistry<br/>注册中心实现"]
end
subgraph "datavines-registry-api"
Registry["Registry<br/>注册中心接口"]
ConnectionListener["ConnectionListener<br/>连接监听器"]
SubscribeListener["SubscribeListener<br/>订阅监听器"]
end
ZooKeeperClient --> ZooKeeperRegistry
ZooKeeperConfig --> ZooKeeperClient
DefaultEnsembleProvider --> ZooKeeperClient
Registry --> ZooKeeperRegistry
ConnectionListener --> ZooKeeperRegistry
SubscribeListener --> ZooKeeperRegistry
```

**图示来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [DefaultEnsembleProvider.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/DefaultEnsembleProvider.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [Registry.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/Registry.java)

**本节来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

## 核心组件
DataVines的ZooKeeper注册中心实现包含几个核心组件：ZooKeeperClient负责与ZooKeeper服务器的底层通信，ZooKeeperConfig用于配置连接参数，ZooKeeperRegistry实现了服务注册与发现的具体逻辑。

**本节来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

## 架构概述
DataVines的ZooKeeper注册中心采用分层架构设计，上层为注册中心接口，中层为具体实现，底层为ZooKeeper客户端封装。这种设计实现了关注点分离，提高了代码的可维护性和可扩展性。

```mermaid
graph TD
A[应用层] --> B[注册中心接口]
B --> C[ZooKeeper注册中心实现]
C --> D[ZooKeeper客户端封装]
D --> E[ZooKeeper服务器]
style A fill:#f9f,stroke:#333
style B fill:#bbf,stroke:#333
style C fill:#f96,stroke:#333
style D fill:#9f9,stroke:#333
style E fill:#999,stroke:#333
classDef layer fill:#eee,stroke:#999,stroke-width:1px;
class A,B,C,D,E layer;
```

**图示来源**
- [Registry.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/Registry.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)

## 详细组件分析

### ZooKeeper客户端封装分析
ZooKeeperClient类是DataVines对Curator Framework的封装，提供了单例模式的ZooKeeper客户端实例。该类负责管理与ZooKeeper服务器的连接，提供基本的CRUD操作。

```mermaid
classDiagram
class ZooKeeperClient {
-Logger logger
-volatile CuratorFramework client
+getInstance() ZooKeeperClient
+buildClient(ZooKeeperConfig) ZooKeeperClient
+get(String) String
+getChildrenKeys(String) String[]
+isExisted(String) boolean
+persist(String, String) void
+update(String, String) void
+persistEphemeral(String, String) void
+persistEphemeral(String, String, boolean) void
+persistEphemeralSequential(String) void
+remove(String) void
+getClient() CuratorFramework
+close() void
+blockAcquireMutex(String) InterProcessMutex
+blockAcquireMutex(String, long) InterProcessMutex
}
class ZooKeeperConfig {
-String serverList
-int baseSleepTimeMs
-int maxSleepMs
-int maxRetries
-int sessionTimeoutMs
-int connectionTimeoutMs
-String digest
+ZooKeeperConfig()
+ZooKeeperConfig(String)
+ZooKeeperConfig(String, int, int, int)
+getServerList() String
+setServerList(String) void
+getBaseSleepTimeMs() int
+setBaseSleepTimeMs(int) void
+getMaxSleepMs() int
+setMaxSleepMs(int) void
+getMaxRetries() int
+setMaxRetries(int) void
+getSessionTimeoutMs() int
+setSessionTimeoutMs(int) void
+getConnectionTimeoutMs() int
+setConnectionTimeoutMs(int) void
+getDigest() String
+setDigest(String) void
}
class DefaultEnsembleProvider {
-String serverList
+DefaultEnsembleProvider(String)
+start() void
+getConnectionString() String
+close() void
+setConnectionString(String) void
+updateServerListEnabled() boolean
}
ZooKeeperClient --> ZooKeeperConfig : "使用"
ZooKeeperClient --> DefaultEnsembleProvider : "使用"
ZooKeeperClient --> CuratorFramework : "封装"
```

**图示来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java#L39-L224)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java#L19-L104)
- [DefaultEnsembleProvider.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/DefaultEnsembleProvider.java#L23-L55)

**本节来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [DefaultEnsembleProvider.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/DefaultEnsembleProvider.java)

### ZooKeeper注册中心实现分析
ZooKeeperRegistry类实现了Registry接口，提供了基于ZooKeeper的服务注册与发现功能。该类利用ZooKeeper的临时节点特性来实现服务的心跳检测和故障剔除。

```mermaid
classDiagram
class ZooKeeperRegistry {
-ZooKeeperClient zooKeeperClient
-Map~String, ConnectionListener~ connectionListeners
-Map~String, SubscribeListener~ subscribeListeners
+init(Properties) void
+register(ServerInfo) void
+unregister(ServerInfo) void
+subscribe(String, SubscribeListener) void
+unsubscribe(String, SubscribeListener) void
+query(String) ServerInfo[]
+close() void
+getConnectionStatus() ConnectionStatus
+addConnectionListener(ConnectionListener) void
+removeConnectionListener(ConnectionListener) void
}
class Registry {
<<interface>>
+init(Properties) void
+register(ServerInfo) void
+unregister(ServerInfo) void
+subscribe(String, SubscribeListener) void
+unsubscribe(String, SubscribeListener) void
+query(String) ServerInfo[]
+close() void
+getConnectionStatus() ConnectionStatus
+addConnectionListener(ConnectionListener) void
+removeConnectionListener(ConnectionListener) void
}
class ConnectionListener {
<<interface>>
+onConnected() void
+onDisconnected() void
}
class SubscribeListener {
<<interface>>
+onDataChanged(String, ServerInfo[]) void
}
class ServerInfo {
-String host
-int port
-String url
-Map~String, String~ properties
+getHost() String
+setHost(String) void
+getPort() int
+setPort(int) void
+getUrl() String
+setUrl(String) void
+getProperties() Map~String, String~
+setProperties(Map~String, String~) void
}
ZooKeeperRegistry --> Registry : "实现"
ZooKeeperRegistry --> ZooKeeperClient : "依赖"
ZooKeeperRegistry --> ConnectionListener : "管理"
ZooKeeperRegistry --> SubscribeListener : "管理"
ZooKeeperRegistry --> ServerInfo : "使用"
```

**图示来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [Registry.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/Registry.java)
- [ConnectionListener.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/ConnectionListener.java)
- [SubscribeListener.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/SubscribeListener.java)

**本节来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

### 服务注册与发现流程分析
当服务启动时，会通过ZooKeeperRegistry注册自身信息。注册过程使用临时节点，确保服务下线时能自动从注册中心移除。

```mermaid
sequenceDiagram
participant Service as "服务实例"
participant Registry as "ZooKeeperRegistry"
participant Client as "ZooKeeperClient"
participant ZK as "ZooKeeper服务器"
Service->>Registry : register(serverInfo)
Registry->>Client : persistEphemeral(path, serverInfo)
Client->>ZK : CREATE(path, EPHEMERAL)
ZK-->>Client : SUCCESS
Client-->>Registry : SUCCESS
Registry-->>Service : 注册成功
Note over Service,ZK : 服务注册完成
Service->>Registry : unregister(serverInfo)
Registry->>Client : remove(path)
Client->>ZK : DELETE(path)
ZK-->>Client : SUCCESS
Client-->>Registry : SUCCESS
Registry-->>Service : 注销成功
Note over Service,ZK : 服务注销完成
```

**图示来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java#L50-L80)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java#L153-L180)

**本节来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)

### 心跳检测与故障剔除机制分析
ZooKeeper的临时节点特性是实现心跳检测和故障剔除的核心。当服务与ZooKeeper的会话断开时，临时节点会自动被删除，从而实现故障服务的自动剔除。

```mermaid
flowchart TD
A[服务启动] --> B[创建临时节点]
B --> C{ZooKeeper会话保持}
C --> |是| D[节点持续存在]
C --> |否| E[会话超时]
E --> F[ZooKeeper自动删除临时节点]
F --> G[服务从注册中心移除]
D --> H[服务正常运行]
H --> C
style A fill:#f9f,stroke:#333
style B fill:#9f9,stroke:#333
style C fill:#ff9,stroke:#333
style D fill:#9f9,stroke:#333
style E fill:#f96,stroke:#333
style F fill:#f96,stroke:#333
style G fill:#f96,stroke:#333
style H fill:#9f9,stroke:#333
```

**图示来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java#L153-L163)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

**本节来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

### 连接状态监听处理分析
ZooKeeperConnectionStateListener负责监听ZooKeeper客户端的连接状态变化，并通知注册的ConnectionListener。

```mermaid
sequenceDiagram
participant Client as "ZooKeeperClient"
participant Listener as "ConnectionStateListener"
participant Registry as "ZooKeeperRegistry"
participant App as "应用程序"
Client->>Listener : 连接状态变化
Listener->>Registry : 通知状态变化
Registry->>Registry : 遍历connectionListeners
loop 每个监听器
Registry->>Listener : onConnected()/onDisconnected()
end
Listener->>App : 处理连接状态变化
Note over Client,App : 连接状态监听流程
```

**图示来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ConnectionListener.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/ConnectionListener.java)

**本节来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ConnectionListener.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/ConnectionListener.java)

## 依赖分析
DataVines的ZooKeeper注册中心实现依赖于Curator Framework作为ZooKeeper的客户端库，同时遵循SPI机制，允许通过配置切换不同的注册中心实现。

```mermaid
graph TD
A[ZooKeeperRegistry] --> B[Curator Framework]
A --> C[ZooKeeperClient]
C --> D[ZooKeeperConfig]
C --> E[DefaultEnsembleProvider]
A --> F[Registry API]
F --> G[ConnectionListener]
F --> H[SubscribeListener]
style A fill:#f96,stroke:#333
style B fill:#999,stroke:#333
style C fill:#9f9,stroke:#333
style D fill:#9f9,stroke:#333
style E fill:#9f9,stroke:#333
style F fill:#bbf,stroke:#333
style G fill:#bbf,stroke:#333
style H fill:#bbf,stroke:#333
```

**图示来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [Registry.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/Registry.java)

**本节来源**
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [Registry.java](file://datavines-registry/datavines-registry-api/src/main/java/io/datavines/registry/api/Registry.java)

## 性能考虑
在使用ZooKeeper注册中心时，需要注意以下性能调优点：

1. **会话超时配置**：合理设置sessionTimeoutMs参数，避免过短导致频繁重连，过长导致故障检测延迟。
2. **连接重试策略**：通过baseSleepTimeMs、maxSleepMs和maxRetries参数配置指数退避重试策略。
3. **连接超时**：设置合理的connectionTimeoutMs，避免在ZooKeeper不可用时长时间阻塞。
4. **批量操作**：对于大量节点操作，考虑使用事务或批量API减少网络开销。

**本节来源**
- [ZooKeeperConfig.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperConfig.java)
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)

## 故障排除指南
当遇到ZooKeeper注册中心相关问题时，可以参考以下排查步骤：

1. **连接问题**：检查ZooKeeper服务器地址和端口配置是否正确。
2. **认证问题**：如果启用了ACL，确保digest配置正确。
3. **会话超时**：监控ZooKeeper客户端日志，查看是否有频繁的会话超时。
4. **节点冲突**：确保服务注册的路径唯一，避免节点冲突。

**本节来源**
- [ZooKeeperClient.java](file://datavines-common/src/main/java/io/datavines/common/zookeeper/ZooKeeperClient.java)
- [ZooKeeperRegistry.java](file://datavines-registry/datavines-registry-plugins/datavines-registry-zookeeper/src/main/java/io/datavines/registry/plugin/ZooKeeperRegistry.java)

## 结论
DataVines的ZooKeeper注册中心实现通过封装Curator Framework，提供了稳定可靠的服务注册与发现功能。利用ZooKeeper的临时节点特性，实现了自动的心跳检测和故障剔除机制。通过清晰的接口设计和SPI机制，保证了系统的可扩展性和灵活性。合理的配置参数和性能调优建议有助于在生产环境中稳定运行。