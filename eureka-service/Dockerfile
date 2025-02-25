# ========================== STAGE 1: BUILD ==========================
FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Sao chép toàn bộ project, bao gồm pom.xml, src/
COPY . .

# Build JAR, bỏ qua test cho nhanh
RUN mvn clean package -DskipTests


# ========================== STAGE 2: RUNTIME ==========================
FROM eclipse-temurin:21-jdk

WORKDIR /app

# Copy file JAR vừa build từ stage 1
COPY --from=builder /app/target/EurokaServer-0.0.1-SNAPSHOT.jar eureka-server.jar

# Eureka Server nghe cổng 8761
EXPOSE 8761

# Lệnh khởi chạy Eureka khi container start
CMD ["java", "-jar", "eureka-server.jar"]
