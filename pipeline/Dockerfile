FROM gradle:9.4.0-jdk25 AS build
WORKDIR /app

COPY . .

RUN ./gradlew bootJar --no-daemon

FROM eclipse-temurin:25-jre
WORKDIR /app

COPY --from=build /app/build/libs/*.jar app.jar

EXPOSE 8080

ENTRYPOINT ["java", "-jar", "/app/app.jar"]