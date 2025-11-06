# Build stage
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app
COPY pom.xml .
RUN mvn dependency:go-offline
COPY src ./src
RUN mvn clean package -DskipTests

# Run stage
FROM eclipse-temurin:17-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar

# Copy Firebase credentials (optional - will not fail if missing)
COPY firebase-credentials.json* ./ 

# Add curl for healthcheck
RUN apk add --no-cache curl

EXPOSE 8080

ENV SPRING_PROFILES_ACTIVE=docker

ENTRYPOINT ["java", "-jar", "app.jar"]
