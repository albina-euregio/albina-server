SQL script naming convention
-----

<folder number>_<sequence number>_<description>.sql

The Idea behind:

The numbers define the execution order, so that all scripts could be copied to
a directory and successfully executed in the corresponding alphabetical order


Initialize plain MySQL Database with liqubase docker image
-----

```
docker run \
  --rm
  -e INSTALL_MYSQL=true \
  -v <DB_CHANGELOG_DIR>:/liquibase/changelog \
  liquibase/liquibase \
  --url="jdbc:mysql://<DB_HOST>:<DB_PORT>/<DATABASE NAME>" \
  --changeLogFile=changelog-main.xml \
  --username=<DB USER> \
  --password=<DB_PASSWORD> \
  update
```

Links
----

* https://www.liquibase.com/blog/adding-liquibase-on-an-existing-project
