# -------- BUILD STAGE --------
    
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

COPY ../../shared/common /app/shared/common
WORKDIR /app/shared/common
RUN mvn clean install -DskipTests

WORKDIR /app/user-service
COPY pom.xml .
RUN mvn dependency:go-offline

COPY src ./src
RUN mvn clean package -DskipTests

# -------- RUNTIME STAGE --------

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

COPY --from=build /app/user-service/target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]