# Metric Plugin : column_unique

## Description

This metric checks the count of unique rows in a column

## Options

|               name               |  type  |  required  | default value |
|:--------------------------------:|:------:|:----------:|:-------------:|
|      [table](#table-string)      | string |    yes     |       -       |
|     [column](#column-string)     | string |    yes     |       -       |

### table [string]
table name

### column [string]
table column need to check

## Example

> POST localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_unique",
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