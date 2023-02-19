# OverView

DataVines is a data observability system designed to help companies reduce "data downtime" by enabling companies to detect data errors in time before they lead to losses, with features such as data catalog management, data quality monitoring, and SLAs. The following is its architectural design diagram:

![DataVines Architecture Diagram](../../img/architecture.jpg)

# Component details
## DataVinesServer
DataVinesServer is the core service of the DataVines platform. It adopts a decentralized design and supports unlimited expansion. Mainly responsible for providing external APIs, scheduling and executing various data monitoring tasks.

### The main function
- Api service
- Platform data storage
- Data source management
- Job scheduling and execution
- Metric management
- Issue management
- SLAs management

### High Availability Design
- DatavinesServer adopts a decentralized design and supports dynamic capacity expansion. All nodes in the cluster can provide services to the outside world, and nodes obtain jobs by competing for distributed locks for scheduling and execution

## MetaDataManager
MetaDataServer is a metadata management center, mainly responsible for metadata-related functions such as data catalog, metadata model, metadata storage, and query.

## MetaDataFetcher 
MetaDataFetcher is mainly responsible for regularly grabbing the metadata information of the specified data source, and then updating it to the metadata storage engine

## NotificationServer
NotificationServer is mainly responsible for alarms. Users configure alarm rules on the platform. Once the data monitoring jobExecution triggers an alarm, NotificationServer will send the error message to the specified platform, such as email, enterprise WeChat, etc.

# Core design
## Plug-in design
### Connector plug-in design
- The Connector module defines interfaces including but not limited to connection parameters, metadata information acquisition, and data source execution scripts, which are used to implement functions such as connecting to data sources, acquiring metadata information, and executing corresponding scripts.
- The plug-in design allows users to implement the corresponding interface of the Connector module to add custom data sources
### Metric plug-in design
- Metric is a very important part of the platform. It is mainly used in various data monitoring tasks. The richness of Metric makes the platform's monitoring and inspection methods more abundant. The platform will have a variety of built-in metrics so that users can use it out of the box, and also support users to implement Metric-related interfaces to add user-specific metrics.
### Engine plug-in design
- Engine is the core component of the platform and defines the execution logic of data monitoring tasks in the computing engine. Different engines correspond to different execution engines, such as Spark, Flink, and Presto. The execution logic is mainly composed of Sources, Transformers, and Sinks. Source performs data source connection and data reading, Transformer performs various aggregation statistics processing, and Sink performs the output of execution results. Different computing engines have different implementations, but the core process is to read data, perform statistics and output data. The plug-in design allows users to customize the Engine to add new computing engines.
# Process Design
## Task execution process
![jobExecution execution flow](../../img/jobExecution-execute-flow.png)

## Data monitoring parameter generation process
![Parameter construction and conversion process](../../img/engine-config-parser.png)
