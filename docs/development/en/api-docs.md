# Task

> POST /api/v1/jobExecution/submit

- spark engine request parameter
```json
{
    "name":"test",
    "executePlatformType":"local",
    "engineType":"spark",
    "engineParameter":{
        "programType":"JAVA",
        "deployMode":"cluster",
        "driverCores":1,
        "driverMemory":"512M",
        "numExecutors":2,
        "executorMemory":"2G",
        "executorCores":2,
        "others":"--conf spark.yarn.maxAppAttempts=1"
    },
    "parameter":{
        "metricType":"null",
        "metricParameter":{
            "table":"person",
            "column":"age"
        },
        "srcConnectorParameter":{
            "type":"mysql",
            "parameters":{
                "database":"test",
                "password":"test",
                "port":"3306",
                "host":"127.0.0.1",
                "user":"test",
                "properties":"useUnicode=true&characterEncoding=UTF-8"
            }
        },
        "expectedType":"fix_value",
        "expectedParameter":{
            "expected_value":10
        },
        "result_formula":"percentage",
        "operator":"eq",
        "threshold":0,
        "failure_strategy":"none"
        
    },
    "retryTimes":0,
    "retryInterval":1000,
    "timeout":10,
    "tenantCode":"ods",
    "env":"export SPARK_HOME2=/opt/cloudera/parcels/SPARK2/lib/spark2"
}
```
- jdbc engine request parameter
```
{
    "name":"test",
    "parameter":{
        "metricType":"value_between",
        "metricParameter":{
            "table":"person",
            "column":"age",
            "min":5,
            "max":80
        },
        "srcConnectorParameter":{
            "type":"mysql",
            "parameters":{
                "database":"test",
                "password":"123456",
                "port":"3306",
                "host":"127.0.0.1",
                "user":"root",
                "properties":"useUnicode=true&characterEncoding=UTF-8"
            }
        }
    }
}
```
- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskId": 1511355300065992706
    }
}
```

> GET /api/v1/jobExecution/result/{taskId}

- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "jobExecutionResult": {
            "id": 15,
            "metricName": "value_between",
            "metricDimension": "completeness",
            "metricType": "single_table",
            "taskId": 1511355300065992706,
            "actualValue": 4.0,
            "expectedValue": 22.0,
            "expectedType": "table_total_rows",
            "resultFormula": "percentage",
            "operator": "gt",
            "threshold": 0.0,
            "failureStrategy": "none",
            "state": "failure",
            "createTime": "2022-04-05T22:49:19",
            "updateTime": "2022-04-05T22:49:19"
        }
    }
}
```

> GET /api/v1/jobExecution/status/{taskId}

- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskStatus": "success"
    }
}
```

> DELETE /api/v1/jobExecution/kill/{id}
- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskId": 1511355300065992706
    }
}
```

# Metric
> GET /api/v1/metric/list

- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "metrics": [
            "custom_sql",
            "null",
            "value_between"
        ]
    }
}
```

> GET /api/v1/metric/info/{name}

- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "metricInfo": {
            "name": "null",
            "type": "single_table",
            "dimension": "completeness",
            "invalidateItemsCanOutput": true,
            "invalidateItems": {
                "sql": "select * from ${table}",
                "resultTable": "invalidate_items",
                "errorOutput": true
            },
            "configList": [
                "table",
                "filter",
                "column"
            ],
            "actualValue": {
                "sql": "select count(*) as actual_value from ${invalidate_items_table}",
                "resultTable": "invalidate_count",
                "errorOutput": false
            },
            "actualName": "actual_name"
        }
    }
}
```