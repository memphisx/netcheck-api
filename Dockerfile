FROM maven:3.6-adoptopenjdk-11-openj9 AS builder
WORKDIR /var/app/src/netcheck/
COPY ./ ./
RUN mvn versions:set -DnewVersion=`cat ./version.txt` && mvn versions:commit
RUN mvn clean install -DskipTests

FROM adoptopenjdk:11-jre-openj9
WORKDIR /var/app/netcheck/
COPY --from=builder /var/app/src/netcheck/target/netcheck.jar ./netcheck.jar
EXPOSE 8080 8081
CMD ["java", "-jar", "/var/app/netcheck/netcheck.jar"]