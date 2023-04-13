FROM alpine AS builder

RUN mkdir -p /webapps/albina
COPY target/*.war /tmp/albina.war
RUN unzip /tmp/albina.war -d /webapps/albina

FROM tomcat:9.0.73-jdk17-temurin

# install required environment dependencies (should we lock the version?)
RUN apt update \
 && apt install -y ghostscript imagemagick webp \
 && apt -y clean \
 && rm -rf /var/lib/apt/list/*

# prepare application folders
RUN mkdir -p /app/logs \
 && mkdir -p /app/static/bulletins \
 && mkdir -p /app/static/pdfs \
 && mkdir -p /app/static/media_files \
 && mkdir -p /app/static/simple \
 && mkdir -p /app/avalanche-warning-maps

# create user
RUN groupadd --gid 1003 -r albina \
 && useradd --uid 1003 -r -g albina -G audio,video albina \
 && chown -R albina:albina /app

# Set java system properties
ENV JAVA_OPTS="-Xms1024m \
 -Xmx1024m \
 -Dfile.encoding=UTF-8 \
 -Djava.awt.headless=true \
 -XX:+ExitOnOutOfMemoryError"

# Configure default log parameters
ENV ALBINA_LOG_PREFIX=""
ENV ALBINA_LOG_FILE="/app/logs/albina.logs"
ENV ALBINA_LOG_LEVEL="INFO"
ENV ALBINA_LOG_SENTRY_MIN_EVENT_LEVEL="WARN"
ENV ALBINA_LOG_LEVEL_SQL="WARN"
ENV ALBINA_LOG_LEVEL_CONNECTIONPOOL="WARN"
ENV ALBINA_LOG_LEVEL_BLOGCONTROLLER="DEBUG"
ENV ALBINA_LOG_LEVEL_RAPIDMAILPROCESSORCONTROLLER="DEBUG"
ENV ALBINA_LOG_LEVEL_PUSHNOTIFICATIONUTIL="DEBUG"
ENV ALBINA_LOG_LEVEL_AVALANCHEBULLETINSERVICE="DEBUG"
ENV ALBINA_LOG_LEVEL_EMAILUTIL="DEBUG"
# disable appender with value = OFF
ENV ALBINA_LOG_STDOUT_MIN_LEVEL="ALL"
ENV ALBINA_LOG_FILE_MIN_LEVEL="OFF"
ENV ALBINA_LOG_SENTRY_MIN_LEVEL="OFF"

# Configure default DB (force application configuration via environment)
ENV ALBINA_DB_CONNECTION_USERNAME="albian";
ENV ALBINA_DB_CONNECTION_PASSWORD="changeit"
ENV ALBINA_DB_CONNECTIONPOOL_MAXSIZE=10
ENV ALBINA_DB_CONNECTION_URL="jdbc:mysql://localhost:3306/albina?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

# copy tomcat config
COPY src/main/container/logging.properties /usr/local/tomcat/conf/logging.properties
COPY src/main/container/server.xml /usr/local/tomcat/conf/server.xml

# install application
COPY --from=builder /webapps /usr/local/tomcat/webapps

USER albina
