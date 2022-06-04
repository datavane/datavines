# Quick start
## Environment preparation

Before installing `DataVines`, please make sure the following software is installed on your server
- `Git` to ensure smooth execution of `git clone`
- `JDK`, make sure `jdk >= 8`
- `Maven`, to ensure the smooth packaging of the project (of course, you can also upload it to the server after packaging locally)

## Download code
```shell
git clone https://github.com/datavines-ops/datavines.git
cd datavines
````

## Database preparation
The metadata of `DataVines` is stored in a relational database. Currently, `MySQL` and `PostgreSQL` are supported, and `PostgreSQL` is used by default. The following uses `MySQL` as an example to illustrate the installation steps:
- Create database `datavines`
- Execute the `script/sql/datavines-mysql.sql` script to initialize the database

> The following building also uses `MySQL` as an example


### Build Project

Using `MySQL` as the metadata storage engine requires the following operations

````
vi pom.xml
/mysql-connector-java # Search for mysql-connector-java
Comment out <scope>test</scope>, save and exit
````

Build and unpack

```shell
mvn clean package -Prelease
cd datavines-dist/target
tar -zxvf datavines-1.0.0-SNAPSHOT-bin.tar.gz
````

After the decompression is complete, enter the directory
````
cd datavines-1.0.0-SNAPSHOT-bin
````
Modify configuration information
````
cd conf
vi application.yaml
````
Mainly modify database information
````
spring:
 datasource:
   driver-class-name: com.mysql.jdbc.Driver
   url: jdbc:mysql://127.0.0.1:3306/datavines?useUnicode=true&characterEncoding=UTF-8
   username: root
   password: 123456
````
If you use Spark as the execution engine and submit it to Yarn for execution, you need the yarn-related configuration information in common.properties
- standalone mode
````
yarn.mode=standalone
yarn.application.status.address=http://%s:%s/ws/v1/cluster/apps/%s #The first %s needs to be replaced the ip address of yarn
yarn.resource.manager.http.address.port=8088
````
- ha mode
````
yarn.mode=ha
yarn.application.status.address=http://%s:%s/ws/v1/cluster/apps/%s
yarn.resource.manager.http.address.port=8088
yarn.resource.manager.ha.ids=192.168.0.1,192.168.0.2
````

## Start service

````
cd bin
sh datavines-daemon.sh start server mysql
````

Check the log, if there is no error message in the log, and you can see `[INFO] 2022-04-10 12:29:05.447 io.datavines.server.DataVinesServer:[61] - Started DataVinesServer in 3.97 seconds (JVM running for 4.69 )`, it proves that the service has been successfully started

### Submit the task for verification
Currently only supports `API` to submit tasks, you can make requests through Postman or other tools
- Get token
> POST localhost:5600/api/v1/login
````
{
"username": "admin",
"password": "123456"
}
````
- response
````
{
    "msg": "Success",
    "code": 200,
    "data": {
        "id": 1,
        "username": "admin",
        "email": "admin@gmail.com",
        "admin": false
    },
    "token": "eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlbl9jcmVhdGVfdGltZSI6MTY1NDM1MzY2OTU1OCwic3ViIjoiYWRtaW4iLCJ0b2tlbl91c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTY1NDM2MjMwOSwidG9rZW5fdXNlcl9wYXNzd29yZCI6IjEyMzQ1NiJ9.gh4s6sYSrzDBQ_-fTYGZQyuMyxFJKzrBoBHCvAJ2t8ouCdHHv7Pv9SiYnt0DG1wJtLB7MDg5MrMBcmtiwpMIZw"
}
````
- Submit tasks
> POST localhost:5600/api/v1/task/submit
````
Request Body:
{
    "name":"test",
    "parameter":{
        "metricType":"column_null",
        "metricParameter":{
            "table":"person",
            "column":"age"
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
Authorization: 
Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlbl9jcmVhdGVfdGltZSI6MTY1NDM1MzY2OTU1OCwic3ViIjoiYWRtaW4iLCJ0b2tlbl91c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTY1NDM2MjMwOSwidG9rZW5fdXNlcl9wYXNzd29yZCI6IjEyMzQ1NiJ9.gh4s6sYSrzDBQ_-fTYGZQyuMyxFJKzrBoBHCvAJ2t8ouCdHHv7Pv9SiYnt0DG1wJtLB7MDg5MrMBcmtiwpMIZw
````
- response
````
{
    "msg": "Success",
    "code": 200,
    "data": 1
}
````
- Query task status
> GET localhost:5600/api/v1/task/status/{taskId}
````
Authorization:
Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlbl9jcmVhdGVfdGltZSI6MTY1NDM1MzY2OTU1OCwic3ViIjoiYWRtaW4iLCJ0b2tlbl91c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTY1NDM2MjMwOSwidG9rZW5fdXNlcl9wYXNzd29yZCI6IjEyMzQ1NiJ9.gh4s6sYSrzDBQ_-fTYGZQyuMyxFJKzrBoBHCvAJ2t8ouCdHHv7Pv9SiYnt0DG1wJtLB7MDg5MrMBcmtiwpMIZw
````
- response
````
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskStatus": "success"
    }
}
````
If the result of the task is success, then you can query the result of the task
- Query task execution results
> GET localhost:5600/api/v1/task/result/{taskId}
````
Authorization:
Bearer eyJhbGciOiJIUzUxMiJ9.eyJ0b2tlbl9jcmVhdGVfdGltZSI6MTY1NDM1MzY2OTU1OCwic3ViIjoiYWRtaW4iLCJ0b2tlbl91c2VyX25hbWUiOiJhZG1pbiIsImV4cCI6MTY1NDM2MjMwOSwidG9rZW5fdXNlcl9wYXNzd29yZCI6IjEyMzQ1NiJ9.gh4s6sYSrzDBQ_-fTYGZQyuMyxFJKzrBoBHCvAJ2t8ouCdHHv7Pv9SiYnt0DG1wJtLB7MDg5MrMBcmtiwpMIZw
````
- response
````
{
    "msg": "Success",
    "code": 200,
    "data": {
        "taskResult": {
            "id": 15,
            "metricName": "column_null",
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
````