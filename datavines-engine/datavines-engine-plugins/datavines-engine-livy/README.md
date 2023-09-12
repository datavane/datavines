## Livy Engine Guide

### 代码模块

    Livy引擎因为和Spark引擎大同小异，仅在提交状态管理的逻辑上有区别，为了避免冗余代码的产生，在Livy引擎模块只有executor子模块，其他沿用Spark引擎子模块，沿用逻辑通过插件配置机制，在Spark引擎子模块中的META-INF中管理Livy引擎

### 使用指导

    Livy引擎使用跟spark引擎大致流程一致，这里罗列可能存在的区别

- 选择Livy引擎
- 全局参数中配置livy开头的相关信息
- 将spark启动任务在/lib/datavines-engine-spark-core-1.0.0-SNAPSHOT.jar 上传到参数livy.task.jar.lib.path指定的位置
- 将作业任务相关jar上传到参数livy.task.jar.lib.path指定的位置
- 在Livy作业配置Jar具体为 --jar xx.jar,zz.jar
  - 如果自己理解需要上传的jar，自行上传
  - 也可以不指定 --jar 会默认按照 livy.task.jars 配置来执行,需要将相关文件上传
  - 还可以在作业配置的选项参数额外指定jar
- 如果操作的数据源是Hive
  - 默认使用Hive jdbc 通用查询，不需要额外配置
  - 如果spark本身已经支持了hive查询可以选择enableHiveSupport模式
    - 在环境变量envConfig需要添加**enable_spark_hive_support=true必填**
    - 可选hive.metastore.warehouse.dir=hdfs:///datavines/warehouse,hive.metastore.uris=thrift://localhost:9083
    - 环境变量envConfig中以,分割