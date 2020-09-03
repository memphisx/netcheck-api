FROM maven:3.6-adoptopenjdk-11-openj9 AS builder
WORKDIR /var/app/src/netcheck/
COPY pom.xml .
COPY version.txt .
RUN mvn versions:set -DnewVersion="$(cat ./version.txt)" && mvn versions:commit
RUN mvn dependency:go-offline

COPY ./src ./src
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget=1.19.4-1ubuntu2.2 unzip=6.0-21ubuntu1 && \
    wget https://github.com/memphisx/netcheck-frontend/releases/download/0.6.0/spa-release.zip && \
    mkdir ./src/main/resources/static && \
    unzip spa-release.zip -d ./src/main/resources/static && \
    rm spa-release.zip
RUN mvn clean install -DskipTests

FROM adoptopenjdk/openjdk11-openj9:alpine-jre
WORKDIR /var/app/netcheck/
COPY --from=builder /var/app/src/netcheck/target/netcheck.jar ./netcheck.jar
RUN addgroup -S netcheck && adduser -S netcheck -G netcheck && apk --no-cache add curl
USER netcheck
VOLUME /tmp
EXPOSE 8080 8081
ENTRYPOINT ["java", "-Dhibernate.types.print.banner=false", "-noverify", "-jar", "/var/app/netcheck/netcheck.jar"]
HEALTHCHECK --interval=60s --timeout=10s --retries=3 CMD curl -sSL "http://localhost:8080/api/v1/actuator/health" || exit 1