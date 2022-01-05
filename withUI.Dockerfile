ARG ARCH=''
FROM ${ARCH}maven:3-eclipse-temurin-17 AS builder
WORKDIR /var/app/src/netcheck/
COPY pom.xml .
RUN mvn dependency:go-offline

COPY ./src ./src
RUN apt-get update && \
    apt-get install -y --no-install-recommends wget unzip && \
    wget https://github.com/memphisx/netcheck-frontend/releases/download/0.8.2/spa-release.zip && \
    mkdir ./src/main/resources/static && \
    unzip spa-release.zip -d ./src/main/resources/static && \
    rm spa-release.zip
RUN mvn clean install -DskipTests

FROM ${ARCH}eclipse-temurin:17
WORKDIR /var/app/netcheck/
COPY --from=builder /var/app/src/netcheck/target/netcheck.jar ./netcheck.jar
RUN addgroup --system netcheck && adduser --no-create-home --gecos '' --ingroup netcheck --disabled-password netcheck
USER netcheck
VOLUME /tmp
EXPOSE 8080 8081
ENTRYPOINT ["java", "-Dhibernate.types.print.banner=false", "-noverify", "-jar", "/var/app/netcheck/netcheck.jar"]
HEALTHCHECK --interval=60s --timeout=10s --retries=3 CMD curl -sSL "http://localhost:8080/api/v1/actuator/health" || exit 1