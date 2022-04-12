# Metric Plugin : column_length

## Description

This metric checks the count of that the column's value match length

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
table column need to check

### comparator [string]
comparator in sql like [ > >= < <= = <>]

### length [int]
table column length

## Example

> POST localhost:5600/api/v1/task/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"column_length",
        "metricParameter":{
            "table":"task",
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