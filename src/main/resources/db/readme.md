SQL script naming convention
-----

```
<folder number>_<sequence number>_<description>.sql
```

The Idea behind:

The numbers define the execution order, so that all scripts could be copied to
a directory and successfully executed in the corresponding alphabetical order

Automatic DB migration at application start
----

To perform the DB migration automatically at server start, the environment variable ALBINA_DB_RUN_MIGRATION must be set to true.

```
ALBINA_DB_RUN_MIGRATION=true
```

Manual DB migration with liqubase (docker image)
-----

When executing liqubae manually, the Changlog must be in the db directory.
Otherwise, there will be problems if you switch between manual execution and 
automatic execution when starting the server, as the file ID will
no longer match.

```
docker run \
  --rm \
  -e INSTALL_MYSQL=true \
  -v <RESOURCE_DIR>:/liquibase/changelog \
  liquibase/liquibase \
  --url="jdbc:mysql://<DB_HOST>:<DB_PORT>/<DATABASE NAME>" \
  --changeLogFile=db/changelog-main.xml \
  --username=<DB USER> \
  --password=<DB_PASSWORD> \
  update
```

Links
----

* https://www.liquibase.com/blog/adding-liquibase-on-an-existing-project
