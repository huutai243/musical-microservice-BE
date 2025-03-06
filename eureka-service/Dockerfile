## ========================== STAGE 1: BUILD ==========================
#FROM maven:3.9-eclipse-temurin-21 AS builder
#
#WORKDIR /app
#
## Sao chép toàn bộ project, bao gồm pom.xml, src/
#COPY . .
#
## Build JAR, bỏ qua test cho nhanh
#RUN mvn clean package -DskipTests
#
#
## ========================== STAGE 2: RUNTIME ==========================
#FROM eclipse-temurin:21-jdk
#
#WORKDIR /app
#
## Copy file JAR vừa build từ stage 1
#COPY --from=builder /app/target/EurokaServer-0.0.1-SNAPSHOT.jar eureka-server.jar
#
## Eureka Server nghe cổng 8761
#EXPOSE 8761
#
## Lệnh khởi chạy Eureka khi container start
#CMD ["java", "-jar", "eureka-server.jar"]

## ========================== STAGE 1: BUILD ==========================
#FROM maven:3.9.4-eclipse-temurin-21 AS builder
#WORKDIR /app
#
## Copy pom.xml trước để cache dependency
#COPY pom.xml ./
#RUN mvn dependency:go-offline -B
#
## Copy source code
#COPY src ./src
#
## Build JAR, bỏ qua test
#RUN mvn clean package -DskipTests
#
## ========================== STAGE 2: RUNTIME ==========================
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#
## Copy file JAR từ stage builder
#COPY --from=builder /app/target/EurokaServer-0.0.1-SNAPSHOT.jar eureka-server.jar
#
## Eureka chạy cổng 8761
#EXPOSE 8761
#
## Lệnh khởi chạy Spring Boot
#CMD ["java", "-jar", "eureka-server.jar"]

# ========================== STAGE 1: BUILD ==========================
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml và tải trước dependencies để cache hiệu quả
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR, bỏ qua test để tăng tốc
RUN mvn clean package -DskipTests

# ========================== STAGE 2: RUNTIME ==========================
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy file JAR từ builder stage
COPY --from=builder /app/target/EurokaServer-0.0.1-SNAPSHOT.jar eureka-server.jar

# Thiết lập biến môi trường để tối ưu hiệu suất Java
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Mở cổng 8761 cho Eureka Server
EXPOSE 8761

# Chạy ứng dụng với Java tối ưu
CMD ["sh", "-c", "java $JAVA_OPTS -jar eureka-server.jar"]



