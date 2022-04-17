# 快速上手
## 环境准备

在安装`DataVines`之前请确保你的服务器上已经安装下面软件
- `Git`，确保 `git clone`的顺利执行
- `JDK`，确保 `jdk >= 8`
- `Maven`, 确保项目的顺利打包（当然你也可以在本地打包以后上传至服务器）

## 下载代码
```shell
git clone https://github.com/datavines-ops/datavines.git
cd datavines
```

## 数据库准备
`DataVines` 的元数据是存储在关系型数据库中，目前支持 `MySQL` 和 `PostgreSQL` ，默认使用 `PostgreSQL` ，下面以`MySQL`为例说明安装步骤：
- 创建数据库 `datavines`
- 执行 `script/sql/datavines-mysql.sql` 脚本进行数据库的初始化

> 下面的项目构建也是以`MySQL`为例


### 项目构建

使用`MySQL`做为元数据存储引擎需要执行以下操作

```
vi pom.xml
/mysql-connector-java # 搜索 mysql-connector-java
将 <scope>test</scope> 注释掉，保存并退出
```

打包并解压

```shell
mvn clean package -Prelease
cd datavines-dist/target
tar -zxvf datavines-1.0.0-SNAPSHOT-bin.tar.gz
```

解压完成以后进入目录
```
cd datavines-1.0.0-SNAPSHOT-bin
```
修改配置信息
```
cd conf
vi application.yaml
```
主要是修改数据库信息
```
spring:
 datasource:
   driver-class-name: com.mysql.jdbc.Driver
   url: jdbc:mysql://127.0.0.1:3306/datavines?useUnicode=true&characterEncoding=UTF-8
   username: root
   password: 123456
```
如果你是使用Spark做为执行引擎，并且是提交到Yarn上面去执行的，那么需要在common.properties中配置yarn相关的信息
- standalone 模式
```
yarn.mode=standalone
yarn.application.status.address=http://%s:%s/ws/v1/cluster/apps/%s #第一个%s需要被替换成yarn的ip地址
yarn.resource.manager.http.address.port=8088
```
- ha 模式
```
yarn.mode=ha
yarn.application.status.address=http://%s:%s/ws/v1/cluster/apps/%s
yarn.resource.manager.http.address.port=8088
yarn.resource.manager.ha.ids=192.168.0.1,192.168.0.2
```

## 启动服务

```
cd bin
sh datavines-daemon.sh start server mysql
```

查看日志，如果日志里面没有报错信息，并且能看到`[INFO] 2022-04-10 12:29:05.447 io.datavines.server.DataVinesServer:[61] - Started DataVinesServer in 3.97 seconds (JVM running for 4.69)`的时候，证明服务已经成功启动

### 提交任务进行验证
目前只支持`API`提交任务，可以通过Postman或者其他工具来进行请求
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







