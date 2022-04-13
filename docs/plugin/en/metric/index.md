# Metric Plugins
## Column Level

|          Name           | JDBC | Spark |               doc                | 
|:-----------------------:|:----:|:-----:|:--------------------------------:|
|       column_null       | yes  |  yes  |      [doc](column_null.md)       |
|     column_not_null     | yes  |  yes  |    [doc](column_not_null.md)     |
|     column_in_enums     | yes  |  yes  |    [doc](column_in_enums.md)     |
|      column_length      | yes  |  yes  |     [doc](column_length.md)      | 
|   column_match_regex	   | yes  |  yes  |   [doc](column_match_regex.md)   | 
| column_match_not_regex	 | yes  |  yes  | [doc](column_match_not_regex.md) | 
|     column_unique	      | yes  |  yes  |     [doc](column_unique.md)      | 
|    column_duplicate	    | yes  |  yes  |    [doc](column_duplicate.md)    | 
|  column_value_between	  | yes  |  yes  |  [doc](column_value_between.md)  | 
|    column_custom_sql    | yes  |  yes  |   [doc](column_custom_sql.md)    | 

## Table Level

|       name       | JDBC | Spark |            doc            | 
|:----------------:|:----:|:-----:|:-------------------------:|
| table_row_count	 | yes  |  yes  | [doc](table_row_count.md) | 
| table_freshness  | yes  |  yes  | [doc](table_freshness.md) | 
