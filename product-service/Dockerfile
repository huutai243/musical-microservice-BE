# Use official OpenJDK image as base
FROM eclipse-temurin:21-jdk

# Set working directory
WORKDIR /app

# Copy Maven dependencies (caching layer)
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy the application source
COPY src src

# Build the application
RUN ./mvnw package -DskipTests

# Expose application port
EXPOSE 8082

# Set environment variables
ENV SPRING_PROFILES_ACTIVE=docker
ENV EUREKA_CLIENT_SERVICEURL_DEFAULTZONE=http://eureka-service:8761/eureka

# Run the application
CMD ["java", "-jar", "target/product-service-0.0.1-SNAPSHOT.jar"]
