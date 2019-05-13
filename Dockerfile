FROM maven:3.6.0-jdk-8-slim AS BUILDER
COPY src /src/WS-DomainValidator/src
COPY pom.xml /src/WS-DomainValidator
COPY maven-eclipse-codestyle.xml /src/WS-DomainValidator
COPY license_header_plain.txt /src/WS-DomainValidator
WORKDIR /src

RUN apt update \
    && apt-get upgrade -y \
    && apt install -y git \
    && rm -r /var/lib/apt/lists/*

RUN mvn -f /src/WS-DomainValidator/pom.xml clean package


FROM tomcat:alpine
COPY --from=BUILDER /src/WS-DomainValidator/target/WS-DomainValidator-*.war /usr/local/tomcat/webapps/ROOT.war
RUN rm /usr/local/tomcat/webapps/ROOT -r -f
EXPOSE 8080
