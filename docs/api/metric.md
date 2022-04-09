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