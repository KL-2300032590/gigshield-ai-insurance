FROM maven:3.9.9-eclipse-temurin-21-alpine AS build
WORKDIR /build

# Copy entire services directory structure
COPY pom.xml ./pom.xml
COPY common ./common
COPY api-gateway ./api-gateway
COPY risk-engine ./risk-engine
COPY trigger-engine ./trigger-engine
COPY fraud-detection ./fraud-detection
COPY claim-service ./claim-service
COPY payout-service ./payout-service

# Install parent POM and build common module
RUN mvn -N install && mvn -pl common clean install -DskipTests

# Build the payout-service
RUN mvn -pl payout-service clean package -DskipTests

FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY --from=build /build/payout-service/target/*.jar app.jar
EXPOSE 8085
ENTRYPOINT ["java", "-jar", "app.jar"]
