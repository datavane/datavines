You can report a bug, submit a new function enhancement suggestion, or submit a pull request directly.

## Submit an Issue

 - Before submitting an issue, please go through a comprehensive search to make sure the problem cannot be solved just by searching.
 - Check the [Issue List](https://github.com/datavane/datavines/issues) to make sure the problem is not repeated.
 - [Create](https://github.com/datavane/datavines/issues/new/choose) a new issue and choose the type of issue.
 - Define the issue with a clear and descriptive title.
 - Fill in necessary information according to the template.
 - Please pay attention for your issue, you may need provide more information during discussion.

## Developer Flow

**1. Prepare repository**

Go to [DataVines GitHub Repo]( https://github.com/datavane/datavines ) and fork repository to your account.

Clone repository to local machine.
```shell
git clone https://github.com/(your_github_name)/datavines.git
```

Add DataVines remote repository.
```shell
git remote add upstrem https://github.com/datavane/datavines.git
git remote -v
```

**2. Choose Issue**

 - Please choose the issue to be edited. If it is a new issue discovered or a new function enhancement to offer, please create an issue and set the right label for it.
 - After choosing the relevant issue, please reply with a deadline to indicate that you are working on it.

**3. Create Branch**

 - Switch to forked master branch, update local branch, then create a new branch.

```shell
git checkout dev
git fetch upstream
git rebase upstream/dev
git push origin dev # optional
git checkout -b issueNo
```

**4. Coding**

  - Please obey the [Code of Conduct](code-conduct.md) during the process of development and finish the check before submitting the pull request.
  - push code to your fork repo.

```shell
git add .
git commit -m 'commit log'
git push origin issueNo
```

**5. Submit Pull Request**

 - Send a pull request to the master branch.
 - The other developer will do code review before discussing some details (including the design, the implementation and the performance) with you. The request will be merged into the branch of current development version after the edit is well enough.
 - At last, congratulations on being an contributor of DataVines

**6. Delete Branch**

 - You can delete the remote branch (origin/issueNo) and the local branch (issueNo) associated with the remote branch (origin/issueNo) after the mentor merged the pull request into the master branch of DataVines.
 
```shell
git checkout dev
git branch -d issueNo
git remote prune origin # If you delete branch on GitHub PR page, else you could delete origin branch with following command
git push origin --delete issueNo
```
**Notice**:  Please note that in order to show your id in the contributor list, don't forget the configurations below:

```shell
git config --global user.name "username"
git config --global user.email "username@mail.com"
```

**Thanks**
This contribution guide refers to the contribution guide of Apache ShardingSphere, thanks to them for their excellent contributions.
