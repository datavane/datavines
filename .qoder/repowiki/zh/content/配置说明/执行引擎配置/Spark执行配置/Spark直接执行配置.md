# Spark直接执行配置

<cite>
**本文档引用文件**   
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkConstants.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkConstants.java)
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkRuntimeEnvironment.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\SparkRuntimeEnvironment.java)
- [SparkDataVinesBootstrap.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-core\src\main\java\io\datavines\engine\spark\core\SparkDataVinesBootstrap.java)
- [SparkDataProfileMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkDataProfileMetricBuilder.java)
- [SparkSingleTableMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkSingleTableMetricBuilder.java)
- [SparkMultiTableValueComparisonMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkMultiTableValueComparisonMetricBuilder.java)
- [ProgramType.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\ProgramType.java)
- [SparkSinkSqlBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkSinkSqlBuilder.java)
- [CommonConstants.java](file://datavines-common\src\main\java\io\datavines\common\CommonConstants.java)
- [SparkEngineParameter.java](file://datavines-common\src\main\java\io\datavines\common\entity\SparkEngineParameter.java)
</cite>

## 目录
1. [简介](#简介)
2. [核心组件](#核心组件)
3. [配置构建流程](#配置构建流程)
4. [Spark参数设置](#spark参数设置)
5. [资源分配策略](#资源分配策略)
6. [队列配置](#队列配置)
7. [执行环境初始化](#执行环境初始化)
8. [配置示例](#配置示例)
9. [性能调优建议](#性能调优建议)
10. [常见问题解决方案](#常见问题解决方案)

## 简介
DataVines通过SparkEngineExecutor直接提交Spark任务，实现数据质量检查和数据剖析等功能。本配置文档详细说明了如何通过BaseSparkConfigurationBuilder构建配置，设置Spark参数，分配资源，配置队列以及初始化执行环境。文档涵盖了从配置构建到任务执行的完整流程，并提供配置示例、性能调优建议和常见问题解决方案，确保用户能够正确配置和优化Spark直接执行模式。

## 核心组件

DataVines中Spark直接执行的核心组件包括SparkEngineExecutor、BaseSparkConfigurationBuilder、SparkParameters和SparkRuntimeEnvironment。SparkEngineExecutor负责构建和执行Spark命令，BaseSparkConfigurationBuilder负责构建任务配置，SparkParameters定义了Spark任务的参数，而SparkRuntimeEnvironment则负责初始化Spark执行环境。

```mermaid
classDiagram
class SparkEngineExecutor {
+init(JobExecutionRequest, Logger, Configurations)
+execute()
+after()
+getProcessResult()
+getTaskRequest()
-buildCommand()
}
class BaseSparkConfigurationBuilder {
+getEnvConfig()
+getSourceConfigs()
+buildSinkConfigs()
+buildTransformConfigs()
+getValidateResultDataSinkConfig(ExpectedValue, String, String, Map)
+getOutputTable(String, String, String)
+getTableAlias(String, String, String, String)
}
class SparkParameters {
+mainJar : String
+mainClass : String
+deployMode : String
+mainArgs : String
+driverCores : int
+driverMemory : String
+numExecutors : int
+executorCores : int
+executorMemory : String
+appName : String
+queue : String
+others : String
+programType : ProgramType
+sparkVersion : String
+jars : String
}
class SparkRuntimeEnvironment {
+sparkSession : SparkSession
+streamingContext : StreamingContext
+config : Config
+enableSparkHiveSupport : boolean
+setConfig(Config)
+getConfig()
+checkConfig()
+prepare()
+createSparkConf()
+createStreamingContext()
+sparkSession()
+streamingContext()
+enableSparkHiveSupport()
+getExecution()
}
SparkEngineExecutor --> BaseSparkConfigurationBuilder : "使用"
SparkEngineExecutor --> SparkParameters : "使用"
SparkEngineExecutor --> SparkRuntimeEnvironment : "使用"
BaseSparkConfigurationBuilder --> SparkSinkSqlBuilder : "使用"
SparkRuntimeEnvironment --> SparkBatchExecution : "创建"
```

**图表来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkRuntimeEnvironment.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\SparkRuntimeEnvironment.java)
- [SparkSinkSqlBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkSinkSqlBuilder.java)

**章节来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkRuntimeEnvironment.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\SparkRuntimeEnvironment.java)

## 配置构建流程

DataVines中的Spark任务配置构建流程由BaseSparkConfigurationBuilder及其子类负责。配置构建器根据任务类型（如数据剖析、单表质量检查、多表值比较等）构建相应的配置。配置构建流程包括环境配置、数据源配置、转换配置和接收器配置的构建。

```mermaid
flowchart TD
Start([开始]) --> GetEnvConfig["构建环境配置"]
GetEnvConfig --> GetSourceConfigs["构建数据源配置"]
GetSourceConfigs --> BuildTransformConfigs["构建转换配置"]
BuildTransformConfigs --> BuildSinkConfigs["构建接收器配置"]
BuildSinkConfigs --> End([结束])
subgraph "环境配置"
GetEnvConfig --> EnableHiveSupport{"启用Spark Hive支持?"}
EnableHiveSupport --> |是| SetHiveSupport["设置enableSparkHiveSupport=true"]
EnableHiveSupport --> |否| Continue1
end
subgraph "数据源配置"
GetSourceConfigs --> ExtractTables["从SQL中提取表名"]
ExtractTables --> ValidateTables{"表名存在?"}
ValidateTables --> |否| ThrowException["抛出DataVinesException"]
ValidateTables --> |是| ProcessTables["处理表名和别名"]
ProcessTables --> AddSourceConfig["添加数据源配置"]
end
subgraph "接收器配置"
BuildSinkConfigs --> GetMetricType["获取指标类型"]
GetMetricType --> IsCustomSql{"自定义SQL?"}
IsCustomSql --> |是| UseCustomSql["使用自定义SQL构建接收器"]
IsCustomSql --> |否| UseDefaultSql["使用默认SQL构建接收器"]
UseCustomSql --> SetSaveMode["设置保存模式为UPSERT"]
UseDefaultSql --> SetSaveMode
end
```

**图表来源**
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)
- [SparkDataProfileMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkDataProfileMetricBuilder.java)
- [SparkSingleTableMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkSingleTableMetricBuilder.java)
- [SparkMultiTableValueComparisonMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkMultiTableValueComparisonMetricBuilder.java)

**章节来源**
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)
- [SparkDataProfileMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkDataProfileMetricBuilder.java)
- [SparkSingleTableMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkSingleTableMetricBuilder.java)
- [SparkMultiTableValueComparisonMetricBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\SparkMultiTableValueComparisonMetricBuilder.java)

## Spark参数设置

Spark参数设置通过SparkParameters类进行，该类定义了Spark任务的所有参数。参数设置包括程序类型、Spark版本、主类、JAR包路径、部署模式、驱动程序和执行器的资源配置等。

```mermaid
classDiagram
class SparkParameters {
+mainJar : String
+mainClass : String
+deployMode : String
+mainArgs : String
+driverCores : int
+driverMemory : String
+numExecutors : int
+executorCores : int
+executorMemory : String
+appName : String
+queue : String
+others : String
+programType : ProgramType
+sparkVersion : String
+jars : String
}
class ProgramType {
+JAVA
+SCALA
+PYTHON
}
SparkParameters --> ProgramType : "包含"
```

**图表来源**
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [ProgramType.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\ProgramType.java)

**章节来源**
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [ProgramType.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\ProgramType.java)

## 资源分配策略

DataVines中的资源分配策略通过Spark参数进行配置，包括驱动程序和执行器的CPU核心数和内存大小。资源分配策略直接影响任务的性能和稳定性。

```mermaid
flowchart TD
Start([开始]) --> SetDriverCores["设置驱动程序核心数"]
SetDriverCores --> SetDriverMemory["设置驱动程序内存"]
SetDriverMemory --> SetNumExecutors["设置执行器数量"]
SetNumExecutors --> SetExecutorCores["设置执行器核心数"]
SetExecutorCores --> SetExecutorMemory["设置执行器内存"]
SetExecutorMemory --> End([结束])
subgraph "驱动程序资源配置"
SetDriverCores --> ValidateDriverCores{"核心数>0?"}
ValidateDriverCores --> |是| AddDriverCores["添加--driver-cores参数"]
ValidateDriverCores --> |否| SkipDriverCores["跳过"]
SetDriverMemory --> ValidateDriverMemory{"内存非空?"}
ValidateDriverMemory --> |是| AddDriverMemory["添加--driver-memory参数"]
ValidateDriverMemory --> |否| SkipDriverMemory["跳过"]
end
subgraph "执行器资源配置"
SetNumExecutors --> ValidateNumExecutors{"数量>0?"}
ValidateNumExecutors --> |是| AddNumExecutors["添加--num-executors参数"]
ValidateNumExecutors --> |否| SkipNumExecutors["跳过"]
SetExecutorCores --> ValidateExecutorCores{"核心数>0?"}
ValidateExecutorCores --> |是| AddExecutorCores["添加--executor-cores参数"]
ValidateExecutorCores --> |否| SkipExecutorCores["跳过"]
SetExecutorMemory --> ValidateExecutorMemory{"内存非空?"}
ValidateExecutorMemory --> |是| AddExecutorMemory["添加--executor-memory参数"]
ValidateExecutorMemory --> |否| SkipExecutorMemory["跳过"]
end
```

**图表来源**
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkConstants.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkConstants.java)

**章节来源**
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkConstants.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkConstants.java)

## 队列配置

队列配置用于指定Spark任务提交到YARN的哪个队列。队列配置有助于资源管理和任务调度。

```mermaid
sequenceDiagram
participant UI as "用户界面"
participant Executor as "SparkEngineExecutor"
participant ArgsUtils as "SparkArgsUtils"
participant Params as "SparkParameters"
UI->>Executor : 提交任务配置
Executor->>Executor : 解析SparkParameters
Executor->>ArgsUtils : 调用buildArgs()
ArgsUtils->>Params : 获取队列名称
Params-->>ArgsUtils : queue
ArgsUtils->>ArgsUtils : 检查部署模式和队列参数
ArgsUtils->>ArgsUtils : 添加--queue参数
ArgsUtils-->>Executor : 返回参数列表
Executor->>Executor : 构建完整命令
Executor-->>UI : 返回执行结果
```

**图表来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)

**章节来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)

## 执行环境初始化

执行环境初始化由SparkRuntimeEnvironment负责，包括SparkSession的创建、Hive支持的启用、流式处理上下文的创建等。

```mermaid
flowchart TD
Start([开始]) --> SetConfig["设置配置"]
SetConfig --> CheckHiveSupport["检查是否启用Hive支持"]
CheckHiveSupport --> |是| EnableHive["启用Hive支持"]
CheckHiveSupport --> |否| SkipHive["跳过"]
EnableHive --> CreateSparkSession["创建SparkSession"]
SkipHive --> CreateSparkSession
CreateSparkSession --> CreateStreamingContext["创建流式处理上下文"]
CreateStreamingContext --> SetExecution["设置执行器"]
SetExecution --> End([结束])
subgraph "SparkSession创建"
CreateSparkSession --> CreateConf["创建SparkConf"]
CreateConf --> SetCrossJoin["设置spark.sql.crossJoin.enabled=true"]
SetCrossJoin --> BuildSession["构建SparkSession"]
BuildSession --> GetOrCreate["getOrCreate()"]
end
subgraph "流式处理上下文创建"
CreateStreamingContext --> GetDuration["从配置获取批处理持续时间"]
GetDuration --> CreateContext["创建StreamingContext"]
CreateContext --> SetContext["设置streamingContext"]
end
```

**图表来源**
- [SparkRuntimeEnvironment.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\SparkRuntimeEnvironment.java)
- [SparkBatchExecution.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\batch\SparkBatchExecution.java)

**章节来源**
- [SparkRuntimeEnvironment.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\SparkRuntimeEnvironment.java)
- [SparkBatchExecution.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-api\src\main\java\io\datavines\engine\spark\api\batch\SparkBatchExecution.java)

## 配置示例

以下是一个完整的Spark任务配置示例，展示了如何设置各种参数：

```json
{
  "programType": "JAVA",
  "deployMode": "cluster",
  "driverCores": 2,
  "driverMemory": "4g",
  "numExecutors": 5,
  "executorCores": 4,
  "executorMemory": "8g",
  "appName": "DataVines-Quality-Check",
  "queue": "default",
  "others": "--conf spark.serializer=org.apache.spark.serializer.KryoSerializer --conf spark.sql.adaptive.enabled=true",
  "sparkVersion": "2.4",
  "jars": "--jars hdfs://path/to/dependency1.jar,hdfs://path/to/dependency2.jar"
}
```

**章节来源**
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [SparkEngineParameter.java](file://datavines-common\src\main\java\io\datavines\common\entity\SparkEngineParameter.java)

## 性能调优建议

为了优化Spark任务的性能，建议采取以下措施：

1. **合理分配资源**：根据数据量和计算复杂度合理设置执行器数量、核心数和内存大小。
2. **启用自适应查询执行**：通过`spark.sql.adaptive.enabled=true`启用自适应查询执行，优化查询计划。
3. **使用Kryo序列化**：通过`spark.serializer=org.apache.spark.serializer.KryoSerializer`使用Kryo序列化，提高序列化性能。
4. **调整并行度**：根据集群资源和数据量调整`spark.sql.shuffle.partitions`参数。
5. **启用动态资源分配**：通过`spark.dynamicAllocation.enabled=true`启用动态资源分配，提高资源利用率。

```mermaid
flowchart TD
Start([开始]) --> AssessDataSize["评估数据量和计算复杂度"]
AssessDataSize --> AllocateResources["合理分配资源"]
AllocateResources --> EnableAQE["启用自适应查询执行"]
EnableAQE --> UseKryo["使用Kryo序列化"]
UseKryo --> AdjustParallelism["调整并行度"]
AdjustParallelism --> EnableDynamicAllocation["启用动态资源分配"]
EnableDynamicAllocation --> MonitorPerformance["监控性能"]
MonitorPerformance --> Optimize["持续优化"]
Optimize --> End([结束])
```

**图表来源**
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkConstants.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkConstants.java)

**章节来源**
- [SparkArgsUtils.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkArgsUtils.java)
- [SparkConstants.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkConstants.java)

## 常见问题解决方案

### 问题1：任务提交失败，提示"Application application_XXXXXX failed 2 times due to AM Container for X failed"
**解决方案**：检查驱动程序内存是否足够，增加`driverMemory`参数的值。

### 问题2：执行器频繁失败
**解决方案**：检查执行器内存是否足够，增加`executorMemory`参数的值，或减少`executorCores`以降低内存压力。

### 问题3：任务执行缓慢
**解决方案**：增加执行器数量(`numExecutors`)和核心数(`executorCores`)，或优化SQL查询。

### 问题4：无法连接到Hive metastore
**解决方案**：确保`enableSparkHiveSupport`设置为`true`，并检查Hive配置是否正确。

### 问题5：自定义JAR包未生效
**解决方案**：确保`jars`参数正确指定了JAR包路径，并使用`--jars`前缀。

```mermaid
flowchart TD
Start([问题]) --> IdentifyIssue["识别问题类型"]
IdentifyIssue --> CheckResources["检查资源分配"]
CheckResources --> AdjustMemory["调整内存设置"]
AdjustMemory --> CheckConfiguration["检查配置"]
CheckConfiguration --> VerifyJars["验证JAR包"]
VerifyJars --> TestConnection["测试连接"]
TestConnection --> ResolveIssue["解决问题"]
ResolveIssue --> End([解决])
subgraph "资源问题"
CheckResources --> InsufficientDriverMemory{"驱动程序内存不足?"}
InsufficientDriverMemory --> |是| IncreaseDriverMemory["增加driverMemory"]
CheckResources --> InsufficientExecutorMemory{"执行器内存不足?"}
InsufficientExecutorMemory --> |是| IncreaseExecutorMemory["增加executorMemory"]
end
subgraph "配置问题"
CheckConfiguration --> HiveSupportEnabled{"Hive支持启用?"}
HiveSupportEnabled --> |否| EnableHiveSupport["设置enableSparkHiveSupport=true"]
VerifyJars --> JarsCorrect{"JAR包路径正确?"}
JarsCorrect --> |否| FixJarsPath["修正JAR包路径"]
end
```

**图表来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)

**章节来源**
- [SparkEngineExecutor.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\SparkEngineExecutor.java)
- [SparkParameters.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-executor\src\main\java\io\datavines\engine\spark\executor\parameter\SparkParameters.java)
- [BaseSparkConfigurationBuilder.java](file://datavines-engine\datavines-engine-plugins\datavines-engine-spark\datavines-engine-spark-config\src\main\java\io\datavines\engine\spark\config\BaseSparkConfigurationBuilder.java)