FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /app
ADD src src
ADD pom.xml pom.xml
RUN mvn --batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true --activate-profiles env-docker package -Dmaven.test.skip=true

FROM tomcat:8-jre11-temurin
COPY --from=build /app/target/albina.war /usr/local/tomcat/webapps/

EXPOSE 8080
