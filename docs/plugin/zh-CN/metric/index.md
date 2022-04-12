# Metric 插件指南

## Metric插件列表
### Column

|         插件名称          | JDBC 引擎 | Spark引擎 |              详细信息               | 
|:---------------------:|:-------:|:-------:|:-------------------------------:|
|      column_null      |   yes   |   yes   |     [点击查看](column_null.md)      |
|    column_in_enums    |   yes   |   yes   |   [点击查看](column_in_enums.md)    |
|    column_not_null    |   yes   |   yes   |   [点击查看](column_not_null.md)    |
|     column_length     |   yes   |   yes   |    [点击查看](column_length.md)     | 
|     column_regex	     |   yes   |   yes   |  [点击查看](column_match_regex.md)  | 
|    column_unique	     |   yes   |   yes   |    [点击查看](column_unique.md)     | 
| column_value_between	 |   yes   |   yes   | [点击查看](column_value_between.md) | 
|   column_custom_sql   |   yes   |   yes   |  [点击查看](column_custom_sql.md)   | 
|   column_freshness    |   yes   |   yes   |   [点击查看](column_freshness.md)   | 


### Table
|       插件名称       | JDBC引擎 | Spark引擎 |            详细信息            | 
|:----------------:|:------:|:-------:|:--------------------------:|
| table_row_count	 |  yes   |   yes   | [点击查看](table_row_count.md) | 
## 开发新的Metric