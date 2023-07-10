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
 && chmod -R 400 ${CATALINA_HOME}/conf/* \
 && chown -R albina:albina /app \
 && chown -R albina:albina ${CATALINA_HOME}

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
# disable appender with value = OFF
ENV ALBINA_LOG_STDOUT_MIN_LEVEL="ALL"
ENV ALBINA_LOG_FILE_MIN_LEVEL="OFF"
ENV ALBINA_LOG_SENTRY_MIN_LEVEL="OFF"
# disable sentry SDK
ENV SENTRY_DSN=""

# Configure default DB (force application configuration via environment)
ENV ALBINA_DB_CONNECTION_USERNAME="changeit";
ENV ALBINA_DB_CONNECTION_PASSWORD="changeit"
ENV ALBINA_DB_CONNECTIONPOOL_MAXSIZE=10
ENV ALBINA_DB_CONNECTION_URL="jdbc:mysql://localhost:3306/changeit?allowPublicKeyRetrieval=true&useSSL=false&useUnicode=true&useJDBCCompliantTimezoneShift=true&useLegacyDatetimeCode=false&serverTimezone=UTC";

# copy tomcat config
COPY --chown=albina:albina --chmod=400 src/main/container/logging.properties ${CATALINA_HOME}/conf/logging.properties
COPY --chown=albina:albina --chmod=400 src/main/container/server.xml ${CATALINA_HOME}/conf/server.xml

# install application
COPY --chown=albina:albina --from=builder /webapps ${CATALINA_HOME}/webapps

USER albina