# DataVines 环境搭建指南

## 前置条件

在搭建 DataVines 开发环境之前请确保你已经安装一下软件

* [Git](https://git-scm.com/downloads): 版本控制系统
* [JDK](https://www.oracle.com/technetwork/java/javase/downloads/index.html): 后端开发
* [Maven](http://maven.apache.org/download.cgi): Java包管理系统

### 克隆代码库

通过你 git 管理工具下载 git 代码，下面以 git-core 为例

```
mkdir datavines
cd datavines
git clone https://github.com/datavines-ops/datavines.git
```
### 编译源码 

* 如果使用MySQL数据库，请注意修改pom.xml，将 `mysql-connector-java` 依赖的 `scope` 改为 `compile`，使用 PostgreSQL 则不需要。
* 运行 ` mvn clean install -Prelease -Dmaven.test.skip=true ` 

### 数据库

DataVines 的元数据存储在关系型数据库中，目前支持的关系型数据库包括 MySQL 以及 PostgreSQL。下面以MySQL为例，启动数据库并创建新 database 作为 DataVines 元数据库，这里以数据库名 datavines 为例

创建完新数据库后，将 `script/sql/datavines-mysql.sql` 下的 sql 文件直接在 MySQL 中运行，完成数据库初始化

## 启动后端

下面步骤将引导如何启动 DataVines 后端服务

### 必要的准备工作

* 打开项目：使用开发工具打开项目，这里以 Intellij IDEA 为例，打开后需要一段时间，让 Intellij IDEA 完成以依赖的下载

* 必要的修改
  * 如果使用 MySQL 作为元数据库，需要先修改 `datavines/pom.xml`，将 `mysql-connector-java` 依赖的 `scope` 改为 `compile`，使用 PostgreSQL 则不需要
  * 修改数据库配置，修改 `datavines-server/src/main/resources/application.yaml` 文件中的数据库配置

  本样例以 MySQL 为例，其中数据库名为 datavines，账户名密码为 root/123456
  ```application.yaml
   spring:
     datasource:
       driver-class-name: com.mysql.jdbc.Driver
       url: jdbc:mysql://127.0.0.1:3306/datavines?useUnicode=true&characterEncoding=UTF-8
       username: root
       password: 123456
  ```

### 启动服务

启动 `DataVinesServer`

> 在VM Options中添加
 - `-Dspring.profiles.active=mysql` 中 `mysql` 表示指定的配置文件
 - `-Dlogging.config=classpath:server-logback.xml` 
 
当你在Console里面看到 `[INFO] 2022-04-10 12:29:05.447 io.datavines.server.DataVinesServer:[61] - Started DataVinesServer in 3.97 seconds (JVM running for 4.69)` 日志的时候，证明服务已经启动成功了

### 提交任务
目前只支持`API`提交任务
- 提交任务
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
- 查询任务状态
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
如果任务的结果为success，那么就可以查询任务的结果
- 查询任务执行结果
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