FROM eclipse-temurin:17-jdk-focal

WORKDIR /app

# Copy the project files
COPY . .

# Make the mvnw script executable
RUN chmod +x mvnw

# Build the application
RUN ./mvnw clean package -DskipTests --no-transfer-progress

# Run the application
ENTRYPOINT ["java", "-jar", "target/spring-producer-0.0.1-SNAPSHOT.jar"] 