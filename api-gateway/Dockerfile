# Giai đoạn build: sử dụng Maven với Java 21 để build ứng dụng
FROM maven:3.9-eclipse-temurin-21 AS builder
WORKDIR /app
# Copy toàn bộ mã nguồn vào container
COPY . .
# Build ứng dụng, bỏ qua tests
RUN mvn clean package -DskipTests

# Giai đoạn chạy: sử dụng Eclipse Temurin với JDK 21 để chạy ứng dụng
FROM eclipse-temurin:21-jdk
WORKDIR /app
# Copy file JAR từ giai đoạn build mà không đổi tên
COPY --from=builder /app/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway-0.0.1-SNAPSHOT.jar
EXPOSE 9000
ENTRYPOINT ["java", "-jar", "api-gateway-0.0.1-SNAPSHOT.jar"]
