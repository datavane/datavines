# Metric Plugin : column_not_in_enums

## Description

This metric checks the count of that column's value in the enumerated list

## Options

|               name               |  type  |  required  | default value |
|:--------------------------------:|:------:|:----------:|:-------------:|
|      [table](#table-string)      | string |    yes     |       -       |
|     [column](#column-string)     | string |    yes     |       -       |
| [enum_list](#enum_list-string)   | string |     no     |       -       |
### table [string]
need metric table

### column [string]
table column need to check

### enum_list [string]
enum_list value like `'1','2','3'` or `1,2,3`

## Example

> POST localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_not_in_enums",
        "metricParameter":{
            "table":"jobExecution",
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