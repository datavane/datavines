# Pull Request 须知

## 前言
Pull Request 本质上是一种软件的合作方式，是将涉及不同功能的代码，纳入主干的一种流程。这个过程中，可以进行讨论、审核和修改代码。

在 Pull Request 中尽量不讨论代码的实现方案，代码及其逻辑的大体实现方案应该尽量在
Issue 或者邮件列表中被讨论确定，在 Pull Request 中我们尽量只关注代码的格式以及代码规范等信息，从而避免实现方式的意见不同而导致
waste time。

## 规范

### Pull Request 标题

标题格式：[`Pull Request 类型`-`Issue 号`][`模块名`] `Pull Request 描述`

其中`Pull Request 类型`和`Issue 类型`的对应关系如下：

|          Issue 类型          | Pull Request 类型 |样例（假设 Issue 号为 1111） | 
|:-----------------------:|:-------:|:-------:|
|       Feature       |   Feature   |   [Feature-1111][server] Implement xxx   |
|     Bug     |   Fix   |   [Fix-1111][server] Fix xxx   |
|     Improvement     |   Improvement   |   [Improvement-1111][alert] Improve the performance of xxx   |
|   Test   |   Test   |   [Test-1111][api] Add the e2e test of xxx   |
|      Sub-Task      |   Sub-Task 对应的父类型   |   [Feature-1111][server] Implement xxx  |


其中 `Issue 号`是指当前 Pull Request 对应要解决的 Issue 号，`模块名`同 Issue 的模块名。

### Pull Request 分支名

分支名格式：`Pull Request 类型`-`Issue 号`，举例：Feature-1111。
