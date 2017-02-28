FROM maven:3.3.9-jdk-8-alpine

COPY ./ /opt/app/
COPY ./searchcode.sqlite.empty /opt/app/searchcode.sqlite
COPY ./searchcode.properties.example /opt/app/searchcode.properties
WORKDIR /opt/app/
RUN mvn test --batch-mode --quiet
