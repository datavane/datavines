> POST /api/v1/task/submit

- spark engine request parameter
```
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
            "src_table":"person",
            "src_column":"age"
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
        "threshold":"0",
        "failure_strategy":none
        
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
            "src_table":"person",
            "src_column":"age",
            "src_min":5,
            "src_max":80
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

> GET /api/v1/task/result/{taskId}

- response
```
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskResult": {
            "id": 15,
            "metricName": "value_between",
            "metricDimension": "completeness",
            "metricType": "single_table",
            "taskId": 1511355300065992706,
            "actualValue": 4.0,
            "expectedValue": 22.0,
            "expectedType": "src_table_total_rows",
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

> GET /api/v1/task/status/{taskId}

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

> DELETE /api/v1/task/kill/{id}
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