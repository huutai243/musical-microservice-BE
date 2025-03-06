## ========================== STAGE 1: BUILD ==========================
## Sử dụng Maven + Temurin Java 21 để build
#FROM maven:3.9-eclipse-temurin-21 AS builder
#
## Khai báo thư mục làm việc cho container
#WORKDIR /app
#
## Sao chép tất cả mã nguồn và pom.xml vào container
#COPY . .
#
## Chạy lệnh Maven để build JAR (bỏ qua test)
#RUN mvn clean package -DskipTests
#
#
## ========================== STAGE 2: RUN ==========================
## Sử dụng image Java 21 nhẹ hơn để chạy ứng dụng
#FROM eclipse-temurin:21-jdk
#
## Tạo thư mục /app cho container runtime
#WORKDIR /app
#
## Sao chép file JAR từ STAGE 1
#COPY --from=builder /app/target/AuthService-0.0.1-SNAPSHOT.jar auth-service.jar
#
## Mở cổng 8081 để ứng dụng lắng nghe
#EXPOSE 8081
#
## Lệnh khởi chạy Spring Boot khi container start
#CMD ["java", "-jar", "auth-service.jar"]

## ========================== STAGE 1: BUILD ==========================
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
#COPY --from=builder /app/target/AuthService-0.0.1-SNAPSHOT.jar auth-service.jar
#
## Cổng 8081
#EXPOSE 8081
#
#CMD ["java", "-jar", "auth-service.jar"]
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
COPY --from=builder /app/target/AuthService-0.0.1-SNAPSHOT.jar auth-service.jar

# Giảm bộ nhớ sử dụng của JVM
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Cổng 8081 cho Auth Service
EXPOSE 8081

# Chạy ứng dụng với Java tối ưu
CMD ["sh", "-c", "java $JAVA_OPTS -jar auth-service.jar"]


