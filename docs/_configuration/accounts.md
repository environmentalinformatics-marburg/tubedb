---
title: "Accounts"
---

In `realm.properties`-file at TubeDB root folder user accounts are listed.  
Settings and accounts are read at TubeDB start time. To apply changes you need to save changes in `tsdb_paths.ini`-file and in `realm.properties`-file, stop TubeDB and then start TubeDB.

Per default no login is needed for TubeDB web-interface.  
To activate login, set in [`tsdb_paths.ini`](../tubedb/) the entry `WEB_SERVER_LOGIN`

activated login:  
`WEB_SERVER_LOGIN = true`  

no login:  
`WEB_SERVER_LOGIN = false`  

no login: (commented out, use default)  
`# WEB_SERVER_LOGIN = false`  
`# WEB_SERVER_LOGIN = true`  

---
### `realm.properties` description

One account per line specifies user-name, password and access-roles.

Special role `admin` provides full access. Role names of project names provide access to stations of that project.

`USER_NAME:PASSOWRD,ROLES`


example `realm.properties`-file with projects `BE` and `project1`. `bob` is permitted to access both projects.
~~~ properties
bob:mypassword,admin
user1:password1,BE
user2:password2,project1
user3:password3,BE,project1
~~~