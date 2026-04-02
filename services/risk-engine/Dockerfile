FROM maven:3.9.9-eclipse-temurin-21 AS build

WORKDIR /app

# copy shared module
COPY services/common ./common

# build shared module first
WORKDIR /app/common
RUN mvn clean install -DskipTests

# copy service
WORKDIR /app
COPY services/risk-engine ./risk-engine

# build service
WORKDIR /app/risk-engine
RUN mvn clean package -DskipTests

FROM maven:3.9.9-eclipse-temurin-21
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
