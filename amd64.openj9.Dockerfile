FROM ibm-semeru-runtimes:open-17-jre
WORKDIR /var/app/netcheck/
COPY ./target/netcheck.jar ./netcheck.jar
RUN addgroup --system netcheck && adduser --no-create-home --gecos '' --ingroup netcheck --disabled-password netcheck
USER netcheck
VOLUME /tmp
EXPOSE 8080 8081
ENTRYPOINT ["java", "-Dhibernate.types.print.banner=false", "-noverify", "-jar", "/var/app/netcheck/netcheck.jar"]
HEALTHCHECK --interval=60s --timeout=10s --retries=3 CMD curl -sSL "http://localhost:8080/api/v1/actuator/health" || exit 1