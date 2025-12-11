FROM maven:3.9-eclipse-temurin-21 as builder

WORKDIR /app

COPY pom.xml .
RUN mvn dependency:go-offline

COPY monitoring-backend/src ./src

RUN mvn clean package -DskipTests

FROM eclipse-temurin:21-jre

WORKDIR /app

COPY --from=builder /app/target/*.jar app.jar

EXPOSE 8083

ENTRYPOINT ["java", "-jar", "app.jar"]
