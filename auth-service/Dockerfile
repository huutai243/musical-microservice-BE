# Use official OpenJDK image as a base image
FROM openjdk:21-jdk-slim

# Set the working directory in the container
WORKDIR /app

# Copy the application JAR file (ensure this path is correct)
COPY target/AuthService-0.0.1-SNAPSHOT.jar auth-service.jar

# Expose the application port
EXPOSE 8081

# Run the application
ENTRYPOINT ["java", "-jar", "auth-service.jar"]
