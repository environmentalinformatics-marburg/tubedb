---
title: "Accounts List"
---

In `realm.properties`-file at TubeDB root folder user accounts are listed.

---
### `realm.properties` description

One account per line specifies user-name, password and access-roles.

Special role `admin` provides full access. Role names of project names provide access to stations of that project.

`USER_NAME:PASSOWRD,ROLES`


example `realm.properties`-file
~~~ properties
admin:password,admin
user1:password1,BE
user2:password2,KI
user3:password3,BE,KI
~~~