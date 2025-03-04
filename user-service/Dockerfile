# ========================== STAGE 1: BUILD ==========================
# Sử dụng Maven + Temurin Java 21 để build
FROM maven:3.9-eclipse-temurin-21 AS builder

# Khai báo thư mục làm việc cho container
WORKDIR /app

# Sao chép tất cả mã nguồn và pom.xml vào container
COPY . .

# Chạy lệnh Maven để build JAR (bỏ qua test)
RUN mvn clean package -DskipTests


# ========================== STAGE 2: RUN ==========================
# Sử dụng image Java 21 nhẹ hơn để chạy ứng dụng
FROM eclipse-temurin:21-jdk

# Tạo thư mục /app cho container runtime
WORKDIR /app

# Sao chép file JAR từ STAGE 1
COPY --from=builder /app/target/user-service-0.0.1-SNAPSHOT.jar user-service.jar

# Mở cổng 8083 để ứng dụng lắng nghe
EXPOSE 8083

# Lệnh khởi chạy Spring Boot khi container start
CMD ["java", "-jar", "user-service.jar"]
