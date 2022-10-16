# Metric Plugin : table_freshness

## Description

This metric checks data freshness

## Options

|                    name                    |  type  | required | default value |
|:------------------------------------------:|:------:|:--------:|:-------------:|
|           [table](#table-string)           | string |   yes    |       -       |
|          [column](#column-string)          | string |   yes    |       -       |
|      [begin_time](#begin_time-string)      | string |   yes    |       -       |
|   [deadline_time](#deadline_time-string)   | string |   yes    |       -       |
| [datetime_format](#datetime_format-string) | string |   yes    |       -       |


### table [string]
need metric table

### column [string]
table column need to check

### begin_time [string]
data begin time

### deadline_time [string]
data deadline time

### datetime_format [string]
column value datetime format

## Example

> POST localhost:5600/api/v1/jobExecution/submit
```json

{
    "name":"test",
    "parameter":{
        "metricType":"table_freshness",
        "metricParameter":{
            "table":"jobExecution",
            "column":"parameter",
            "begin_time": "2021-02-22",
            "deadline_time": "2021-02-22",
            "datetime_format":"yyyy-MM-dd"
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