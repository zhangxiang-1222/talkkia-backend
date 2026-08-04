# FROM openjdk:11-jre-slim
FROM eclipse-temurin:11-jre
WORKDIR /app
COPY target/talkkia-0.0.1-SNAPSHOT.jar app.jar
RUN mkdir -p /app/secrets
ENV FIREBASE_SERVICE_ACCOUNT_FILE=/app/secrets/firebase-admin.json
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
