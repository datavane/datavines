# Metric Plugin : column_not_null

## Description

This metric checks the count of that the column's value is not null 

## Options

|               name               |  type  |  required  | default value |
|:--------------------------------:|:------:|:----------:|:-------------:|
|      [table](#table-string)      | string |    yes     |       -       |
|     [column](#column-string)     | string |    yes     |       -       |

### table [string]
need metric table

### column [string]
table column need to check

## Example

> POST localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_not_null",
        "metricParameter":{
            "table":"jobExecution",
            "column":"parameter"
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