# Build stage
FROM maven:3.6.3-jdk-11 as build

COPY ./ /home/smartrepo
WORKDIR /home/smartrepo
RUN mvn clean package -Dmaven.test.skip=true


# Package stage
FROM openjdk:11.0.4-jre-slim

COPY --from=build /home/smartrepo/target/smartrepo-fat.jar /opt/verticles/smartrepo.jar
COPY --from=build /home/smartrepo/src/config/docker.json /opt/verticles/

EXPOSE 7070

WORKDIR /opt/verticles

ENTRYPOINT ["sh", "-c"]
CMD ["java -Dvertx.logger-delegate-factory-class-name=io.vertx.core.logging.SLF4JLogDelegateFactory -jar smartrepo.jar -cluster -ha -conf docker.json"]