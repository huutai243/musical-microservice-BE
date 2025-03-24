# ========================== STAGE 1: BUILD ==========================
# Dùng Maven để build project
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml và tải dependencies để cache
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR, bỏ qua test
RUN mvn clean package -DskipTests

# ========================== STAGE 2: RUNTIME ==========================
# Dùng image nhẹ để chạy ứng dụng
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy file JAR từ giai đoạn build
COPY --from=builder /app/target/notification-service-0.0.1-SNAPSHOT.jar notification-service.jar

# Thiết lập biến môi trường Java
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Expose cổng cho Notification Service (8088)
EXPOSE 8088

# Chạy ứng dụng
CMD ["sh", "-c", "java $JAVA_OPTS -jar notification-service.jar"]
