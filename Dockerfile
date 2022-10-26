FROM alpine:latest AS build-albina-admin-gui
WORKDIR /app
ADD https://gitlab.com/albina-euregio/albina-admin-gui/-/jobs/artifacts/environment-relative/download?job=build:production albina-admin-gui.zip
RUN apk add unzip && unzip albina-admin-gui && find

FROM alpine:latest AS build-avalanche-warning-maps
WORKDIR /app
ADD https://gitlab.com/albina-euregio/avalanche-warning-maps/-/archive/master/avalanche-warning-maps-master.zip avalanche-warning-maps.zip
RUN apk add unzip && unzip avalanche-warning-maps.zip && find

FROM maven:3-eclipse-temurin-17 AS build
WORKDIR /app
ADD src src
ADD pom.xml pom.xml
RUN mvn --batch-mode --errors --fail-at-end --show-version -DinstallAtEnd=true -DdeployAtEnd=true --activate-profiles env-docker package -Dmaven.test.skip=true

FROM tomcat:8-jre11-temurin
COPY --from=build /app/target/albina.war /usr/local/tomcat/webapps/
COPY --from=build-albina-admin-gui /app/dist /usr/local/tomcat/webapps/ROOT
COPY --from=build-avalanche-warning-maps /app/avalanche-warning-maps-master /opt/avalanche-warning-maps

EXPOSE 8080
