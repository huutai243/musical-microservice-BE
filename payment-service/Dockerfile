# ========================== STAGE 1: BUILD ==========================
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml để cache dependencies trước
COPY pom.xml ./
RUN mvn dependency:go-offline -B

# Copy toàn bộ mã nguồn để build
COPY src ./src

# Build project, bỏ qua test để nhanh hơn
RUN mvn clean package -DskipTests

# ========================== STAGE 2: RUNTIME ==========================
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy file JAR từ builder stage
COPY --from=builder /app/target/payment-service-0.0.1-SNAPSHOT.jar payment-service.jar

# Thiết lập JAVA_OPTS (tối ưu hiệu suất khi chạy container)
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Mở cổng 8087 đúng với cấu hình server.port
EXPOSE 8087

# Chạy ứng dụng
CMD ["sh", "-c", "java $JAVA_OPTS -jar payment-service.jar"]
