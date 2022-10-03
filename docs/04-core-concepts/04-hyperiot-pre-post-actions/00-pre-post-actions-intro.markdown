# Pre/Post Actions [](id=pre-post-actions)

HyperIoT Framework allows registration to standard system events such as pre-login or post-login or alternatively preSave or postSave in the case of entities to be persisted to a database.

The framework makes a sharp division, however, between the concept of generic Pre/Post Action and those related to database operations.

![Pre/Post Actions Hierarchy](../images/pre-post-actions-hierarchy.png)

In each specific section (authentication, persistence ,etc...) the specific actions available are documented.