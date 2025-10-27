# albina-server

The server stores and processes all relevant information for the ALBINA system such as bulletins.

## Bulletin status/workflow

```mermaid
flowchart TD
    missing --> |edit| draft
    draft --> |submit| submitted
    submitted --> |edit| draft
    submitted --> |publish, automated publication at 5PM| published
    published --> |edit| updated
    updated --> |submit| resubmitted
    resubmitted --> |edit| updated
    resubmitted --> |publish, automated update at 8AM or manually at any time| republished
    republished --> |edit| updated
```

## Translations

This project uses Transifex for its translations: https://app.transifex.com/albina-euregio/albina-server/dashboard/

## Deployment

```shell
mvn versions:set -DgenerateBackupPoms=false -DnewVersion=$(git describe --tags)
mvn clean package
scp target/albina*.jar albina.example.com:/opt/albina-server/albina.jar
sudo systemctl restart albina-server.service
# logging to stdout (no log files), use `journalctl` to view the logs:
journalctl --unit albina-server.service
journalctl --unit albina-server-dev.service --follow --lines 100
```

```properties
# /etc/systemd/system/albina-server.service
[Unit]
After=syslog.target network.target

[Install]
WantedBy=multi-user.target

[Service]
Environment=JDBC_URL=...
Environment=JDBC_USER=...
Environment=JDBC_PASSWORD=...
Environment=JWT_GENERATOR_SIGNATURE_SECRET=...
Environment=MICRONAUT_SERVER_PORT=8080
Environment=UMASK=0022
ExecStart=/usr/lib/jvm/jre-21/bin/java -jar albina.jar
Type=simple
User=...
WorkingDirectory=/opt/albina-server-dev/
```

## Database Migrations
Database migrations are handled by Liquibase, and are enabled by default via `liquibase.datasources.default.change-log=classpath\:db/changelog-main.xml`

### Liquibase for Automatic Database Migrations

1. Apply appropriate hibernate annotations in Java source
2. Generate liquibase changesets (Note: You can also skip steps ii. and iii. and just write the changeset yourself):
   1. extend `resources/hibernate.cfg.xml` if necessary
   2. `mvn compile`
   3. `mvn liquibase:diff` --> this generates the file `resourcees/db/generated_changelog.mariadb.sql`
   4. check the contents of the file and adapt if necessary. Once you are satisfied, rename it, update the lines `-- changeset albina:000_000000 failOnError:true` accordingly, and move into appropriate sub-folder under `resources/db`
3. Deploy changesets in your local test environment:
   1. run `mvn compile` followed by `mvn liquibase:update`, or,
   2. just start the local server with `mvn mn:run`
4. Commit changes to Java source and liquibase changesets together.

### Tips
- If you only want changes for a specific table of the database you can use `diffIncludeObjects`. E.g.
` mvn liquibase:diff -Dliquibase.diffIncludeObjects="regions"`
- similarly, `diffExcludeObjects` can be used to ignore tables
- use `mvn liquibase:status` to check what changes will be applied before you run `mvn liquibase:update`
- The JPA Buddy plugin for Intellij (Ultimate) provides nice GUI features for automatic changeset generation.
  You can specify a connection to your development database and it will detect when columns are missing and automatically
  suggest to generate changesets.
- It is a best practice to specify only one type of change per changeset. Doing so avoids failed auto-commit statements
that can leave the database in an unexpected state. When you deploy your changes, each changeset either succeeds or fails;

## Update CHANGELOG (for new releases)

[git-cliff](https://git-cliff.org/docs/) needs to be installed on your system.

Please use the following workflow when releasing new versions:

1. determine new version number `<TAG>` and
   run `git-cliff -u -p CHANGELOG.md -t <TAG>`
2. edit `CHANGELOG.md` by hand if necessary and commit
3. create `<TAG>` with git

If you forgot to update the changelog before creating a new tag in git, use
`git-cliff -l -p CHANGELOG.md`. This will add all commits for the
**latest** tag to the CHANGELOG. The downside compared to the workflow above is, that the
changes to CHANGELOG itself are not included in the release.

If there have been several new releases since the last update to CHANGELOG,
use e.g. `git-cliff -p CHANGELOG.md v7.0.6..` to prepend all changes that
happened *after* version v7.0.6 was released.

## Development Setup

The `env-local` profile defined in _pom.xml_ is used to configure the server for local development.
Follow these steps to set up and run the server locally.

1. Ensure that a database with the appropriate schema and entries is accessible on port 3306. If the database is hosted remotely and requires an SSH tunnel, use the following command to set up the connection:

```bash
ssh example-server -L 3306:localhost:3306
```

2. Start the server:

```bash
mvn mn:run
```

3. Once the server is running, you can access the API by navigating to: http://0.0.0.0:8080/albina/api

## Micronaut 4.9.2 Documentation

- [User Guide](https://docs.micronaut.io/4.9.2/guide/index.html)
- [API Reference](https://docs.micronaut.io/4.9.2/api/index.html)
- [Configuration Reference](https://docs.micronaut.io/4.9.2/guide/configurationreference.html)
- [Micronaut Guides](https://guides.micronaut.io/index.html)
---

- [Micronaut Maven Plugin documentation](https://micronaut-projects.github.io/micronaut-maven-plugin/latest/)

## Micronaut Data
Micronaut Data is a database access toolkit that uses Ahead of Time (AoT) compilation to pre-compute queries for repository interfaces that are then executed by a thin, lightweight runtime layer.
- [Guide](https://micronaut-projects.github.io/micronaut-data/latest/guide/)

## Feature liquibase documentation

- [Micronaut Liquibase Database Migration documentation](https://micronaut-projects.github.io/micronaut-liquibase/latest/guide/index.html)

- [https://www.liquibase.org/](https://www.liquibase.org/)


## Feature tomcat-server documentation

- [Micronaut Tomcat Server documentation](https://micronaut-projects.github.io/micronaut-servlet/latest/guide/index.html#tomcat)


## Feature maven-enforcer-plugin documentation

- [https://maven.apache.org/enforcer/maven-enforcer-plugin/](https://maven.apache.org/enforcer/maven-enforcer-plugin/)


## Feature jul-to-slf4j documentation

- [https://www.slf4j.org/legacy.html#jul-to-slf4jBridge](https://www.slf4j.org/legacy.html#jul-to-slf4jBridge)


## Feature jdbc-hikari documentation

- [Micronaut Hikari JDBC Connection Pool documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#jdbc)


## Feature serialization-jackson documentation

- [Micronaut Serialization Jackson Core documentation](https://micronaut-projects.github.io/micronaut-serialization/latest/guide/)


## Feature micronaut-aot documentation

- [Micronaut AOT documentation](https://micronaut-projects.github.io/micronaut-aot/latest/guide/)


## Feature jackson-xml documentation

- [Micronaut Jackson XML serialization/deserialization documentation](https://micronaut-projects.github.io/micronaut-jackson-xml/latest/guide/index.html)

- [https://github.com/FasterXML/jackson-dataformat-xml](https://github.com/FasterXML/jackson-dataformat-xml)


## Feature hibernate-jpa documentation

- [Micronaut Hibernate JPA documentation](https://micronaut-projects.github.io/micronaut-sql/latest/guide/index.html#hibernate)
