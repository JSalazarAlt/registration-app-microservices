FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY ../../shared/common /app/shared/common
WORKDIR /app/shared/common
RUN mvn clean install -DskipTests
WORKDIR /app/authservice
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/authservice/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]