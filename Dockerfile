#
# Multi-stage Docker build for the Account Receivable Spring Boot service.
#

FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./
COPY src ./src

RUN chmod +x gradlew \
    && ./gradlew bootJar --no-daemon


FROM eclipse-temurin:25-jre AS runtime
WORKDIR /app

ENV JAVA_OPTS=""

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
