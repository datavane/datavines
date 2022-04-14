# Metric Plugin : column_in_enums

## Description

This metric checks the count of that column's value in the enumerated list

## Options

|              name              |  type  | required | default value |
|:------------------------------:|:------:|:--------:|:-------------:|
|     [table](#table-string)     | string |   yes    |       -       |
|    [column](#column-string)    | string |   yes    |       -       |
| [enum_list](#enum_list-string) | string |   yes    |       -       |
### table [string]
need metric table

### column [string]
table column need to check

### enum_list [string]
enum_list value like `'1','2','3'` or `1,2,3`

## Example

> POST localhost:5600/api/v1/task/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_in_enums",
        "metricParameter":{
            "table":"task",
            "column":"parameter",
            "enum_list": "'1','2','3'"
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