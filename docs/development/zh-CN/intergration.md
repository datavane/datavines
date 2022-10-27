# 定义运行参数
 
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
        "metricType":"column_not_in_enums",
        "metricParameter":{
            "table":"test_table",
            "column":"test_column",
            "enum_list":"2,4",
            "database":"test_database"
        },
        "srcConnectorParameter":{
            "type":"mysql",
            "parameters":{
                "database":"test_database",
                "password":"123456",
                "port":"3306",
                "host":"localhost",
                "user":"root",
                "properties":"useUnicode=true&characterEncoding=UTF-8&useSSL=false"
            }
        },
        "expectedType":"fix_value",
        "expectedParameter":{
            "expected_value":"1"
        },
        "resultFormula":"diff",
        "operator":"gt",
        "threshold":5
    },
    "error_data_storage_type": "local-file",
    "error_data_storage_parameter": {
      "error_data_dir":"/tmp/datavines/error-data"
    },
    "validate_result_storage_type": "local-file",
    "validate_result_storage_parameter":{
      "result_data_file_dir":"/tmp/datavines/validate-result-data"
    }
}
```
- jdbc engine request parameter
```
{
    "name":"test",
    "executePlatformType":"local",
    "parameter":{
        "metricType":"column_not_in_enums",
        "metricParameter":{
            "table":"test_table",
            "column":"test_column",
            "enum_list":"2,4",
            "database":"test_database"
        },
        "srcConnectorParameter":{
            "type":"mysql",
            "parameters":{
                "database":"test_database",
                "password":"123456",
                "port":"3306",
                "host":"localhost",
                "user":"root",
                "properties":"useUnicode=true&characterEncoding=UTF-8&useSSL=false"
            }
        },
        "expectedType":"fix_value",
        "expectedParameter":{
            "expected_value":"1"
        },
        "resultFormula":"diff",
        "operator":"gt",
        "threshold":5
    },
    "error_data_storage_type": "local-file",
    "error_data_storage_parameter": {
      "error_data_dir":"/tmp/datavines/error-data"
    },
    "validate_result_storage_type": "local-file",
    "validate_result_storage_parameter":{
      "result_data_file_dir":"/tmp/datavines/validate-result-data"
    }
}
```