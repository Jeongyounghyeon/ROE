FROM eclipse-temurin:24-jdk AS build
WORKDIR /app
COPY gradlew gradlew.bat settings.gradle.kts build.gradle.kts ./
COPY gradle ./gradle
COPY order-service ./order-service
COPY pg-service/build.gradle.kts ./pg-service/build.gradle.kts
RUN ./gradlew :order-service:bootJar --no-daemon

FROM eclipse-temurin:24-jre
WORKDIR /app
COPY --from=build /app/order-service/build/libs/*.jar app.jar
ENTRYPOINT ["java", "--sun-misc-unsafe-memory-access=allow", "-jar", "app.jar"]
