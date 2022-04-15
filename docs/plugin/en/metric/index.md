# Metric Plugins
## Column Level

|          Name           | JDBC | Spark |               doc                | status|
|:-----------------------:|:----:|:-----:|:--------------------------------:|:----:|
|       column_null       | yes  |  yes  |      [doc](column_null.md)       |supported|
|     column_not_null     | yes  |  yes  |    [doc](column_not_null.md)     |supported|
|     column_in_enums     | yes  |  yes  |    [doc](column_in_enums.md)     |supported|
|   column_not_in_enums   | yes  |  yes  |  [doc](column_not_in_enums.md)   |supported|
|      column_length      | yes  |  yes  |     [doc](column_length.md)      |supported|
|   column_match_regex	   | yes  |  yes  |   [doc](column_match_regex.md)   |supported| 
| column_match_not_regex	 | yes  |  yes  | [doc](column_match_not_regex.md) |supported| 
|     column_unique	      | yes  |  yes  |     [doc](column_unique.md)      | supported|
|    column_duplicate	    | yes  |  yes  |    [doc](column_duplicate.md)    |supported| 
|  column_value_between	  | yes  |  yes  |  [doc](column_value_between.md)  | supported|
|    column_custom_sql    | yes  |  yes  |   [doc](column_custom_sql.md)    |supported|

## Table Level

|       name       | JDBC | Spark |            doc            | status|
|:----------------:|:----:|:-----:|:-------------------------:|:----:|
| table_row_count	 | yes  |  yes  | [doc](table_row_count.md) | supported|
| table_freshness  | yes  |  yes  | [doc](table_freshness.md) | supported|

## MultiTable Level
|       name       | JDBC | Spark |            doc            | status|
|:----------------:|:------:|:-------:|:------------------------:|:-------:|
| multi_table_accuracy	 |  no   |   yes   | [doc](multi_table_accuracy.md) | planed|
| multi_table_comparison  |  no   |   yes   | [doc](multi_table_comparison.md) | planed|
