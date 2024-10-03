
FROM openjdk:17-jdk-slim
WORKDIR /app
COPY target/texteditor-1.0-SNAPSHOT.jar /app/texteditor.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "texteditor.jar"]