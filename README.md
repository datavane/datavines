# DataVines

[![EN doc](https://img.shields.io/badge/document-English-blue.svg)](README.md)
[![CN doc](https://img.shields.io/badge/文档-中文版-blue.svg)](README.zh-CN.md)

---

Data quality is used to ensure the accuracy of data in the process of integration and processing. It is also the core component of DataOps. DataVines is an easy-to-use data quality service platform that supports multiple metric.

## Architecture Design
![DataVinesArchitecture](docs/img/architecture.jpg)

## Install

Need: Maven 3.6.1 and later
```sh
$ mvn clean package -Prelease
```
## Features of DataVines

* Easy to use
* Built in multiple Metric、ExpectedType、ResultFormula
  * [Metric Plugins](docs/plugin/en/metric/index.md)
  * [ExpectedType Plugins](docs/plugin/en/expected-value/index.md)
  * [ResultFormula Plugins](docs/plugin/en/result-formula/index.md)

* Modular and plug-in mechanism, easy to extend
  * [Engine Plugins](docs/plugin/en/engine/index.md)
  * [Connector Plugins](docs/plugin/en/connector/index.md)
  * [Register Plugins](docs/plugin/en/register/index.md)
  * [Notification Plugins](docs/plugin/en/notification/index.md)
* Support Spark 2.x、JDBC Engine

## Environmental dependency

1. java runtime environment, java >= 8

2. If you want to run DataVines in a cluster environment, any of the following Spark cluster environments is usable:

- Spark on Yarn
- Spark Standalone

If the data volume is small, or the goal is merely for functional verification, you can also start in local mode without
a cluster environment, because DataVines supports standalone operation. 

## Quick start
[QuickStart](docs/document/en/quick-start.md)

## Development
[DeveloperGuide](docs/development/en/index.md)

## How to Contribute

## Contact Us

## License
[Apache 2.0 License.](LICENSE)