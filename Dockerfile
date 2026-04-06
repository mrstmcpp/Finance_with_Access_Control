FROM eclipse-temurin:21-jdk AS build
WORKDIR /app

# Copy build metadata first to maximize layer caching.
COPY gradlew gradlew
COPY gradlew.bat gradlew.bat
COPY gradle gradle
COPY build.gradle settings.gradle ./

# Warm Gradle dependency cache.
RUN chmod +x gradlew && ./gradlew --no-daemon dependencies > /dev/null

# Copy source and package the executable JAR.
COPY src src
RUN ./gradlew --no-daemon bootJar -x test

FROM eclipse-temurin:21-jre AS runtime
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
