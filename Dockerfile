# ---------- BUILD STAGE ----------
FROM eclipse-temurin:25-jdk AS build
WORKDIR /app

# Copy Gradle wrapper & config
COPY gradlew ./
COPY gradle ./gradle
COPY build.gradle settings.gradle ./

# Copy source
COPY src ./src

# Build JAR
RUN chmod +x gradlew && ./gradlew clean bootJar --no-daemon


# ---------- RUNTIME STAGE ----------
FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
