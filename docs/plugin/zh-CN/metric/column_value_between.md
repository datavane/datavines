# Metric Plugin : column_value_between

## Description

This metric checks the count of value between min and max rows in a column

## Options

|           name           |  type  | required | default value |
|:------------------------:|:------:|:--------:|:-------------:|
|  [table](#table-string)  | string |   yes    |       -       |
| [column](#column-string) | string |   yes    |       -       |
|     [min](#min-int)      | string |    no    |       -       |
|     [max](#max-int)      |  int   |    no    |       -       |

### table [string]
need metric table

### column [string]
table column need to metric length

### min [int]
min value

### max [int]
max value

## Example

> POST localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_value_between",
        "metricParameter":{
            "table":"jobExecution",
            "column":"parameter",
            "min": 0,
            "max": 50
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