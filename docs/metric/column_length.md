# column_length metric


## example

localhost:5600/api/v1/task/submit
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

## table
need metric table

## column
table column need to metric length

## comparator
comparator in sql like [ > >= < <= = <>]

## length
table column length