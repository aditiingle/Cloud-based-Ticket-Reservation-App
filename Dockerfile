# Use official OpenJDK 17 image
FROM eclipse-temurin:17-jdk-alpine

# Set working directory in the container
WORKDIR /app

# Copy Maven wrapper and pom.xml
COPY mvnw .
COPY .mvn .mvn
COPY pom.xml .

# Make mvnw executable
RUN chmod +x mvnw

# Download dependencies (offline mode)
RUN ./mvnw dependency:go-offline

# Copy the rest of the project
COPY src ./src

# Package the app
RUN ./mvnw package -DskipTests

# Expose port 8080 (Spring Boot default)
EXPOSE 8080

# Run the Spring Boot application
CMD java -jar target/*.jar