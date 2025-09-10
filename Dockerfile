FROM openjdk:17-jdk-slim

# Set working directory
WORKDIR /app

# Download OpenTelemetry Java agent
RUN apt-get update && apt-get install -y wget && \
    wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/latest/download/opentelemetry-javaagent.jar -O /app/opentelemetry-javaagent.jar && \
    apt-get remove -y wget && apt-get autoremove -y && rm -rf /var/lib/apt/lists/*

# Copy Maven wrapper and pom.xml
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./

# Download dependencies
RUN ./mvnw dependency:go-offline -B

# Copy source code
COPY src ./src

# Build the application
RUN ./mvnw clean package -DskipTests

# Copy the built jar file
RUN cp target/*.jar app.jar

# Expose port (optional, batch jobs don't typically need exposed ports)
EXPOSE 8080

# Set the entrypoint with OpenTelemetry agent
ENTRYPOINT ["java", "-javaagent:/app/opentelemetry-javaagent.jar", "-jar", "app.jar"]
