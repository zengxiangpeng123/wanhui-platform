# syntax=docker/dockerfile:1.7
FROM maven:3.9-eclipse-temurin-21 AS build
ARG MAVEN_MIRROR_URL=https://maven.aliyun.com/repository/public
WORKDIR /build
COPY maven-settings.docker.xml pom.xml ./
COPY src ./src
RUN --mount=type=cache,target=/root/.m2 mvn -B -q -s maven-settings.docker.xml -DskipTests package

FROM eclipse-temurin:21-jre
RUN groupadd --system wanhui && useradd --system --gid wanhui --home-dir /app wanhui
WORKDIR /app
COPY --from=build /build/target/*.jar app.jar
USER wanhui
EXPOSE 8080
ENV JAVA_OPTS="-XX:MaxRAMPercentage=75 -Dfile.encoding=UTF-8"
ENTRYPOINT ["sh", "-c", "exec java $JAVA_OPTS -jar app.jar"]
