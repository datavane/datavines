# Metric Plugin : column_custom_sql

## Description

This metric is to metric the column length

## Options

|               name               |  type  |  required  | default value |
|:--------------------------------:|:------:|:----------:|:-------------:|
|      [table](#table-string)      | string |    yes     |       -       |
|     [column](#column-string)     | string |    yes     |       -       |
| [comparator](#comparator-string) | string |     no     |       -       |
|      [length](#length-int)       |  int   |     no     |       -       |

### table [string]
need metric table

### column [string]
table column need to metric length

### comparator [string]
comparator in sql like [ > >= < <= = <>]

### length [int]
table column length

## Example

localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_length",
        "metricParameter":{
            "table":"jobExecution",
            "column":"parameter",
            "comparator": ">",
            "length": 50
        },
        "srcConnectorParameter":{
            "type":"postgresql",
            "parameters":{
                "database":"datavines",
                "password":"xxxxxxx",
                "port":"5432",
                "host":"localhost",
                "user":"postgres",
                "properties":"useUnicode=true&characterEncoding=UTF-8"
            }
        }
    }
}
```