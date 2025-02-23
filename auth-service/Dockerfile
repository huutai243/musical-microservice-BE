FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jdk
WORKDIR /app
COPY --from=builder /app/target/AuthService-0.0.1-SNAPSHOT.jar auth-service.jar
EXPOSE 8081
CMD ["java", "-jar", "auth-service.jar"]
