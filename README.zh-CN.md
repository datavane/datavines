# DataVines

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README.zh-CN.md)

---

数据质量是用于保证数据在集成、处理过程中的数据准确性，也是`DataOps`的核心组成部分。DataVines 是一个简单易用、支持多种`Metric`检查的数据质量服务平台。

## 架构设计
![DataVinesArchitecture](docs/img/architecture.jpg)
## 安装

使用Maven3.6.1以及以上版本
```sh
$ mvn clean package -Prelease
```
## DataVines 的特性

* 简单易用
* 内置多种 Metric、Expected Type
* 模块化和插件化，易于扩展
* 支持 Spark 2.x、JDBC 执行引擎

## 环境依赖

1. Java 运行环境，Java >= 8
3. DataVines 支持 JDBC 引擎，如果你的数据量较小或者只是想做功能验证，可以使用 JDBC 引擎
2. 如果您要想要基于 Spark 来运行 DataVines ，那么需要保证你的服务器具有运行 Spark 应用程序的条件

## 快速入门

## 开发指南
请参考官方文档： [开发指南](docs/development/zh-CN/index.md)

## 行为准则

## 欢迎联系

## License
[Apache 2.0 License.](LICENSE)