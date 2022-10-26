FROM alpine:latest AS build-albina-admin-gui
WORKDIR /app
ENV API_BASE_URL="/albina/api/"
ENV WS_BASE_URL="wss://socket.avalanche.report/albina/"
ENV TEXTCAT_URL="/textcat-ng/"
ENV HEADER_BG_COLOR="#ffffff"
ADD https://gitlab.com/albina-euregio/albina-admin-gui/-/jobs/artifacts/master/download?job=build albina-admin-gui.zip
RUN apk add unzip && unzip albina-admin-gui && find
RUN apk add gettext && envsubst < dist/assets/env.template.js > dist/assets/env.js

FROM alpine:latest AS build-textcat-ng
WORKDIR /app
ADD https://gitlab.com/albina-euregio/textcat-ng/-/jobs/artifacts/master/download?job=build textcat-ng.zip
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.CA.txt dist/assets/satzkatalog.CA.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.DE.txt dist/assets/satzkatalog.DE.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.EN.txt dist/assets/satzkatalog.EN.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.ES.txt dist/assets/satzkatalog.ES.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.FR.txt dist/assets/satzkatalog.FR.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.IT.txt dist/assets/satzkatalog.IT.txt
ADD https://admin.avalanche.report/textcat-ng-dev/assets/satzkatalog.OC.txt dist/assets/satzkatalog.OC.txt
RUN apk add unzip && unzip textcat-ng && find

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
RUN apt-get update -y && apt-get install -y ghostscript imagemagick webp
COPY --from=build /app/target/albina.war /usr/local/tomcat/webapps/
COPY --from=build-albina-admin-gui /app/dist /usr/local/tomcat/webapps/ROOT
COPY --from=build-textcat-ng /app/dist /usr/local/tomcat/webapps/textcat-ng
COPY --from=build-avalanche-warning-maps /app/avalanche-warning-maps-master /opt/avalanche-warning-maps

EXPOSE 8080
