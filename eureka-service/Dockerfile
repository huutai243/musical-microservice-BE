# Giai đoạn build: sử dụng Maven với Java 21 để build ứng dụng
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
COPY . .
RUN mvn clean package -DskipTests
# RUN ls -la /app/target

# Giai đoạn chạy: sử dụng image Eclipse Temurin với JDK 21
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy file JAR từ giai đoạn build, đổi tên thành eureka-server.jar
COPY --from=builder /app/target/EurokaServer-0.0.1-SNAPSHOT.jar eureka-server.jar
EXPOSE 8761
ENTRYPOINT ["java", "-jar", "eureka-server.jar"]
