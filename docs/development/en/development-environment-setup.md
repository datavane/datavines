# DataVines Environmental Setup Guide

## Software Requests

Please make sure you have installed the software as follow

* [Git](https://git-scm.com/downloads): 
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html)
* [Maven](http://maven.apache.org/download.cgi)

### Clone Git Repository

```
mkdir datavines
cd datavines
git clone https://github.com/datavines-ops/datavines.git
```
### Compile source code  

* If you use MySQL as your metadata database, you need to modify datavines/pom.xml and change the scope of the mysql-connector-java dependency to compile. This step is not necessary to use PostgreSQL
* Run ` mvn clean install -Prelease -Dmaven.test.skip=true ` 

### Database

The DataVines's metadata is stored in relational database. Currently supported MySQL and Postgresql. We use MySQL as an example. Start the database and create a new database named datavines as DataVines metabase

After creating the new database, run the sql file under`script/sql/datavines-mysql.sql` to complete the database initialization

## Start DataVinesServer

Following steps will guide how to start the DataVinesServer

### Prepare

* Open project: Use IDE open the project, here we use Intellij IDEA as an example

* Configure database related information
  * If you use MySQL as your metadata database, you need to modify datavines/pom.xml and change the scope of the mysql-connector-java dependency to compile. This step is not necessary to use PostgreSQL
  * change database config in  `datavines-server/src/main/resources/application.yaml` 

  For example
  ```application.yaml
   spring:
     datasource:
       driver-class-name: com.mysql.jdbc.Driver
       url: jdbc:mysql://127.0.0.1:3306/datavines?useUnicode=true&characterEncoding=UTF-8
       username: root
       password: 123456
  ```

### Start Service

Start `DataVinesServer`

> add options in VM Options 
 - `-Dspring.profiles.active=mysql` 
 - `-Dlogging.config=classpath:server-logback.xml` 
 
When you see the log that `[INFO] 2022-04-10 12:29:05.447 io.datavines.server.DataVinesServer:[61] - Started DataVinesServer in 3.97 seconds (JVM running for 4.69)` in console，DataVinesServer started successfully

### Submit Task
Only supports submitting tasks via http
- submit task
> POST localhost:5600/api/v1/task/submit
```
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
- query task status
> GET localhost:5600/api/v1/task/status/{taskId}

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
if the task status is success，you can get the task result 

- query task result
> GET localhost:5600/api/v1/task/result/{taskId}

- response
```
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
```