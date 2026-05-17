# syntax=docker/dockerfile:1.7

FROM eclipse-temurin:21-jdk-alpine AS builder
WORKDIR /build

COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw

COPY src ./src
RUN --mount=type=cache,target=/root/.m2 \
    ./mvnw -B -q clean package -DskipTests

RUN mkdir -p target/extracted \
    && java -Djarmode=tools -jar target/*-SNAPSHOT.jar \
         extract --layers --launcher --destination target/extracted

FROM eclipse-temurin:21-jre-alpine AS runtime
WORKDIR /app

RUN addgroup -S spring && adduser -S spring -G spring

ARG EXTRACTED=/build/target/extracted
COPY --from=builder --chown=spring:spring ${EXTRACTED}/dependencies/ ./
COPY --from=builder --chown=spring:spring ${EXTRACTED}/spring-boot-loader/ ./
COPY --from=builder --chown=spring:spring ${EXTRACTED}/snapshot-dependencies/ ./
COPY --from=builder --chown=spring:spring ${EXTRACTED}/application/ ./

USER spring:spring

ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -XX:+UseG1GC -Djava.security.egd=file:/dev/./urandom"
ENV SERVER_PORT=8080
EXPOSE 8080

ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS org.springframework.boot.loader.launch.JarLauncher"]
