FROM gradle:9-jdk AS build
WORKDIR /home/gradle/project
COPY build.gradle settings.gradle gradle.properties ./
COPY gradlew .
COPY gradle gradle
RUN chmod +x ./gradlew || true
COPY src ./src
RUN ./gradlew clean build -x test --no-daemon

FROM eclipse-temurin:21-jre
WORKDIR /app
ARG JAR_FILE=build/libs/*.jar
COPY --from=build /home/gradle/project/${JAR_FILE} app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-Xms512m", "-Xmx1g", "-jar", "app.jar"]