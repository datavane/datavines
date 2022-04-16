您可以报告 bug，提交一个新的功能增强建议或者直接对以上内容提交改进补丁。

## 提交 issue

 - 在提交 issue 之前，请经过充分的搜索，确定该 issue 不是通过简单的检索即可以解决的问题。
 - 查看 [issue 列表](https://github.com/datavines-ops/datavines/issues)，确定该 issue 不是一个重复的问题。
 - [新建](https://github.com/datavines-ops/datavines/issues/new/choose)一个 issue 并选择您的 issue 类型。
 - 使用一个清晰并有描述性的标题来定义 issue。
 - 根据模板填写必要信息。
 - 请对自己提交的 issue 保持关注，在讨论中进一步提供必要信息。

## 开发流程

**1. 准备仓库**

到 [DataVines GitHub Repo]( https://github.com/datavines-ops/datavines ) fork 仓库到你的 GitHub 账号。

克隆到本地。
```shell
git clone https://github.com/(your_github_name)/datavines.git
```

添加 DataVines 远程仓库。
```shell
git remote add upstream https://github.com/datavines-ops/datavines.git
git remote -v
```

**2. 选择 issue**

 - 请在选择您要修改的 issue。如果是您新发现的问题或想提供 issue 中没有的功能增强，请先新建一个 issue 并设置正确的标签。
 - 在选中相关的 issue 之后，请回复以表明您当前正在这个 issue 上工作。并在回复的时候为自己设置一个 deadline，添加至回复内容中。

**3. 创建分支**

 - 切换到 fork 的 dev 分支，拉取最新代码，创建本次的分支。

```shell
git checkout dev
git fetch upstream
git rebase upstream/dev
git push origin dev # 可选操作
git checkout -b issueNo
```

**4. 编码**

 - 请您在开发过程中遵循 DataVines 的[开发规范](code-conduct.md)。并在准备提交 pull request 之前完成相应的检查。
 - 将修改的代码 push 到 fork 库的分支上。

```shell
git add .
git commit -m 'commit log'
git rebase upstream/dev # 必要操作
git push origin issueNo
```

**5. 提交 PR**

 - 发送一个 pull request 到 DataVines 的 dev 分支。
 - 接着会有其他开发者做 CodeReview，然后他会与您讨论一些细节（包括设计，实现，性能等）。当相应的开发者对本次修改满意后，会将提交合并到当前开发版本的分支中。
 - 最后，恭喜您已经成为了 DataVines 的贡献者！

**6. 删除分支**

 - 在将 pull request 合并到 DataVines 的 dev 分支中之后，您就可以将远程的分支（origin/issueNo）及与远程分支（origin/issueNo）关联的本地分支（issueNo）删除。
 
```shell
git checkout dev
git branch -d issueNo
git remote prune origin # 如果你已经在 GitHub PR 页面删除了分支，否则的话可以执行下面的命令删除
git push origin --delete issueNo
```

**注意**: 为了让您的 id 显示在 contributor 列表中，别忘了以下设置：

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```

**感谢**

本贡献指南参考Apache ShardingSphere的贡献指南，感谢他们做出的优秀贡献