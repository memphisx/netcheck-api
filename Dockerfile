FROM maven:3.6-adoptopenjdk-11-openj9 AS builder
WORKDIR /var/app/src/netcheck/
COPY ./ ./
RUN mvn clean install

FROM adoptopenjdk:11-jre-openj9
WORKDIR /var/app/netcheck/
COPY --from=builder /var/app/src/netcheck/target/netcheck-0.0.1-SNAPSHOT.jar ./netcheck.jar
EXPOSE 8080 8081
CMD ["java", "-jar", "/var/app/netcheck/netcheck.jar"]