# Build stage
FROM gradle:8.6-jdk21 AS build
WORKDIR /app
COPY settings.gradle.kts build.gradle.kts ./
# Cache dependencies
RUN gradle dependencies --no-daemon || true
COPY src ./src
RUN gradle bootJar --no-daemon -x test

# Run stage
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/build/libs/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
