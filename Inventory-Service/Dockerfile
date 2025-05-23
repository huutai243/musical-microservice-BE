# ========================== STAGE 1: BUILD ==========================
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml để cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code vào container
COPY src ./src

# Build JAR, bỏ qua test
RUN mvn clean package -DskipTests

# ========================== STAGE 2: RUNTIME ==========================
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy file JAR đã build từ builder stage
COPY --from=builder /app/target/inventory-service-0.0.1-SNAPSHOT.jar inventory-service.jar

# Thiết lập biến môi trường để tối ưu hiệu suất Java
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Cổng 8083 cho Inventory Service
EXPOSE 8083

# Chạy ứng dụng với Java tối ưu
CMD ["sh", "-c", "java $JAVA_OPTS -jar inventory-service.jar"]
