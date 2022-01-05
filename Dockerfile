ARG ARCH=''
FROM ${ARCH}maven:3-eclipse-temurin-17 AS builder
WORKDIR /var/app/src/netcheck/
COPY pom.xml .
COPY version.txt .
RUN mvn versions:set -DnewVersion="$(cat ./version.txt)" && mvn versions:commit
RUN mvn dependency:go-offline

COPY ./src ./src
RUN mvn clean install -DskipTests

FROM ${ARCH}eclipse-temurin:17
WORKDIR /var/app/netcheck/
COPY --from=builder /var/app/src/netcheck/target/netcheck.jar ./netcheck.jar
RUN addgroup -S netcheck && adduser --disabled-password --ingroup netcheck netcheck
USER netcheck
VOLUME /tmp
EXPOSE 8080 8081
ENTRYPOINT ["java", "-Dhibernate.types.print.banner=false", "-noverify", "-jar", "/var/app/netcheck/netcheck.jar"]
HEALTHCHECK --interval=60s --timeout=10s --retries=3 CMD curl -sSL "http://localhost:8080/api/v1/actuator/health" || exit 1