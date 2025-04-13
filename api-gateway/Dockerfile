## ========================== STAGE 1: BUILD ==========================
## Sử dụng Maven + Temurin Java 21 để build
#FROM maven:3.9-eclipse-temurin-21 AS builder
#
## Khai báo thư mục làm việc trong container
#WORKDIR /app
#
## Sao chép toàn bộ project (bao gồm pom.xml, src/) vào container
#COPY . .
#
## Chạy lệnh Maven để build JAR (bỏ qua test để build nhanh hơn)
#RUN mvn clean package -DskipTests
#
#
## ========================== STAGE 2: RUNTIME ==========================
## Sử dụng image Java 21 nhẹ hơn để chạy ứng dụng
#FROM eclipse-temurin:21-jdk
#
## Tạo thư mục /app cho container runtime
#WORKDIR /app
#
## Sao chép file JAR từ STAGE 1
#COPY --from=builder /app/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar
#
## Mở cổng 9000 (khớp với server.port=9000)
#EXPOSE 9000
#
## Lệnh khởi chạy Spring Boot khi container start
#CMD ["java", "-jar", "api-gateway.jar"]

#-------------------------ver2-------------------------------------

### ========================== STAGE 1: BUILD ==========================
#FROM maven:3.9.4-eclipse-temurin-21 AS builder
#WORKDIR /app
#
#COPY pom.xml ./
#RUN mvn dependency:go-offline -B
#
#COPY src ./src
#RUN mvn clean package -DskipTests
#
## ========================== STAGE 2: RUNTIME ==========================
#FROM eclipse-temurin:21-jre
#WORKDIR /app
#
#COPY --from=builder /app/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar
#
## Cổng 9000
#EXPOSE 9000
#
#CMD ["java", "-jar", "api-gateway.jar"]

# ========================== STAGE 1: BUILD ==========================
FROM maven:3.9.4-eclipse-temurin-21-alpine AS builder
WORKDIR /app

# Copy pom.xml trước để cache dependencies
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Copy source code
COPY src ./src

# Build JAR, bỏ qua test
RUN mvn clean package -DskipTests

# ========================== STAGE 2: RUNTIME ==========================
FROM eclipse-temurin:21-alpine
WORKDIR /app

# Copy file JAR từ builder stage
COPY --from=builder /app/target/api-gateway-0.0.1-SNAPSHOT.jar api-gateway.jar

# Giảm bộ nhớ sử dụng của JVM
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Cổng 9000 cho API Gateway
EXPOSE 9000

# Chạy ứng dụng với Java tối ưu
CMD ["sh", "-c", "java $JAVA_OPTS -jar api-gateway.jar"]


