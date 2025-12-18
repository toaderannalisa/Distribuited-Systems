FROM eclipse-temurin:21-jdk-alpine
WORKDIR /app
COPY . /app
RUN ./mvnw clean package -DskipTests
EXPOSE 8091
CMD ["java", "-jar", "target/chatbot-0.0.1-SNAPSHOT.jar", "--server.port=8091"]
