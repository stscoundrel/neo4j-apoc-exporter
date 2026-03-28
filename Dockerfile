# Build stage
FROM maven:3.9.11-eclipse-temurin-25 AS builder

WORKDIR /app

# Build jar
COPY pom.xml .
COPY src ./src
RUN mvn clean package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy built JAR
COPY --from=builder /app/target/neo4j-apoc-exporter-0.1.0.jar app.jar

# Expose default port
EXPOSE 17687

CMD ["java", "-jar", "app.jar"]