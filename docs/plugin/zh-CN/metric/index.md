
# Metric插件列表
## Column 级别

|          插件名称           | JDBC 引擎 | Spark引擎 |              详细信息               |状态| 
|:-----------------------:|:-------:|:-------:|:-------------------------------:|:-------:|
|       column_null       |   yes   |   yes   |      [文档](column_null.md)       |支持|
|     column_not_null     |   yes   |   yes   |    [文档](column_not_null.md)     |支持|
|     column_in_enums     |   yes   |   yes   |    [文档](column_in_enums.md)     |支持|
|   column_not_in_enums   |   yes   |   yes   |  [文档](column_not_in_enums.md)   |支持|
|      column_length      |   yes   |   yes   |     [文档](column_length.md)      | 支持|
|   column_match_regex	   |   yes   |   yes   |   [文档](column_match_regex.md)   | 支持|
| column_match_not_regex	 |   yes   |   yes   | [文档](column_match_not_regex.md) | 支持|
|     column_unique	      |   yes   |   yes   |     [文档](column_unique.md)      | 支持|
|    column_duplicate	    |   yes   |   yes   |    [文档](column_duplicate.md)    | 支持|
|  column_value_between	  |   yes   |   yes   |  [文档](column_value_between.md)  | 支持|
|    column_custom_sql    |   yes   |   yes   |   [文档](column_custom_sql.md)    | 支持|

## Table 级别

|       插件名称       | JDBC引擎 | Spark引擎 |           详细信息           | 状态|
|:----------------:|:------:|:-------:|:------------------------:|:-------:|
| table_row_count	 |  yes   |   yes   | [文档](table_row_count.md) | 支持|
| table_freshness  |  yes   |   yes   | [文档](table_freshness.md) | 支持|

## MultiTable 级别
|       插件名称       | JDBC引擎 | Spark引擎 |           详细信息           | 状态|
|:----------------:|:------:|:-------:|:------------------------:|:-------:|
| multi_table_accuracy	 |  no   |   yes   | [文档](multi_table_accuracy.md) | 计划|
| multi_table_comparison  |  no   |   yes   | [文档](multi_table_comparison.md) | 计划|
