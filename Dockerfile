FROM adoptopenjdk/openjdk11:alpine

RUN apk --update add maven

WORKDIR /opt/app/

COPY ./pom.xml /opt/app
RUN mvn -f ./pom.xml -B dependency:resolve-plugins dependency:resolve clean package

COPY ./ /opt/app/

RUN mvn -Dmaven.test.skip=true package
CMD java -jar ./target/searchcode-1.3.15.jar
