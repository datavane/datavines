# Metric Plugin : column_match_regex

## Description

This metric checks the count of that the column's value match regex pattern

## Options

|           name           |  type  | required | default value |
|:------------------------:|:------:|:--------:|:-------------:|
|  [table](#table-string)  | string |   yes    |       -       |
| [column](#column-string) | string |   yes    |       -       |
| [regexp](#regexp-string) | string |   yes    |       -       |

### table [string]
need metric table

### column [string]
table column need to check

### regexp [string]
regexp is regex pattern

## Example

> POST localhost:5600/api/v1/task/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_match_regex",
        "metricParameter":{
            "table":"task",
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