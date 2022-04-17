# Pull Request Notice

## Preface
Pull Request is a way of software cooperation, which is a process of bringing code involving different functions into the trunk. During this process, the code can be discussed, reviewed, and modified.

In Pull Request, we try not to discuss the implementation of the code. The general implementation of the code and its logic should be determined in Issue. In the Pull Request, we only focus on the code format and code specification, so as to avoid wasting time caused by different opinions on implementation.

## Specification

### Pull Request Title

Title Format: [`Pull Request Type`-`Issue No`][`Module Name`] `Pull Request Description`

The corresponding relationship between `Pull Request Type` and `Issue Type` is as follows:

|          Issue Type          | Pull Request Type |Example(Suppose Issue No is 1111) | 
|:-----------------------:|:-------:|:-------:|
|       Feature       |   Feature   |   [Feature-1111][server] Implement xxx   |
|     Bug     |   Fix   |   [Fix-1111][server] Fix xxx   |
|     Improvement     |   Improvement   |   [Improvement-1111][alert] Improve the performance of xxx   |
|   Test   |   Test   |   [Test-1111][api] Add the e2e test of xxx   |
|      Sub-Task      |   (Parent type corresponding to Sub-Task)   |   [Feature-1111][server] Implement xxx  |


`Issue No` refers to the Issue number corresponding to the current Pull Request to be resolved, `Module Name` is the same as the `Module Name` of Issue.

### Pull Request Branch

Branch name format: `Pull Request type`-`Issue number`. e.g. Feature-3333
