# Multi-stage build for Admin Simulator Service
FROM maven:3.9.9-eclipse-temurin-21-alpine AS build

WORKDIR /build

# Copy parent POM
COPY pom.xml ./pom.xml
COPY common ./common
COPY api-gateway ./api-gateway
COPY risk-engine ./risk-engine
COPY trigger-engine ./trigger-engine
COPY fraud-detection ./fraud-detection
COPY claim-service ./claim-service
COPY payout-service ./payout-service

# Copy all module POMs
COPY admin-simulator/pom.xml admin-simulator/

# Install parent POM
RUN mvn -N install

# Build common module
RUN mvn -pl common install -DskipTests

# Copy admin-simulator source
COPY admin-simulator/ admin-simulator/

# Build admin-simulator service
RUN mvn -pl admin-simulator package -DskipTests

# Runtime stage
FROM eclipse-temurin:21-jdk-alpine

WORKDIR /app

# Copy JAR from build stage
COPY --from=build /build/admin-simulator/target/*.jar app.jar

# Expose port
EXPOSE 8091

# Health check
HEALTHCHECK --interval=30s --timeout=10s --retries=3 \
  CMD wget --no-verbose --tries=1 --spider http://localhost:8091/actuator/health || exit 1

# Run application
ENTRYPOINT ["java", "-jar", "app.jar"]
