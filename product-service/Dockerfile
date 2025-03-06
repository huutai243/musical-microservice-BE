## Use official OpenJDK image as base
#FROM eclipse-temurin:21-jdk
#
## Set working directory
#WORKDIR /app
#
## Copy Maven dependencies (caching layer)
#COPY mvnw .
#COPY .mvn .mvn
#COPY pom.xml .
#RUN chmod +x mvnw && ./mvnw dependency:go-offline -B
#
## Copy the application source
#COPY src src
#
## Build the application
#RUN ./mvnw package -DskipTests
#
## Expose application port
#EXPOSE 8082
#
## Set environment variables
#ENV SPRING_PROFILES_ACTIVE=docker
#ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-service:8761/eureka
#
## Run the application
#CMD ["java", "-jar", "target/product-service-0.0.1-SNAPSHOT.jar"]

# ========================== STAGE 1: BUILD ==========================
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
#COPY --from=builder /app/target/product-service-0.0.1-SNAPSHOT.jar product-service.jar
#
## Cổng 8082
#EXPOSE 8082
#
#CMD ["java", "-jar", "product-service.jar"]
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
COPY --from=builder /app/target/product-service-0.0.1-SNAPSHOT.jar product-service.jar

# Thiết lập biến môi trường để tối ưu hiệu suất Java
ENV JAVA_OPTS="-XX:+UseG1GC -XX:+ExitOnOutOfMemoryError"

# Cổng 8082 cho Product Service
EXPOSE 8082

# Chạy ứng dụng với Java tối ưu
CMD ["sh", "-c", "java $JAVA_OPTS -jar product-service.jar"]


