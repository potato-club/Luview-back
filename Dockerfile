# Step 1: Use a base image with JDK
FROM openjdk:17-jdk-slim

# Step 2: Set the working directory
WORKDIR /app

# Step 3: Copy the jar file (compiled Spring Boot .jar) into the container
COPY target/*.jar /app/app.jar

# Step 4: Expose the application port
EXPOSE 8080

# Step 5: Command to run the Spring Boot application
CMD ["java", "-jar", "/app/app.jar"]
