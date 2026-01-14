# FROM eclipse-temurin:21-jdk-alpine
# VOLUME /tmp
# ARG JAR_FILE=target/*.jar
# COPY ${JAR_FILE} app.jar
# ENTRYPOINT ["java","-jar","/app.jar"]
FROM eclipse-temurin:21-jdk-alpine

# Optional: create non-root user (security best practice)
RUN addgroup -S spring && adduser -S spring -G spring

WORKDIR /app

# Copy the JAR — using the typical naming pattern
COPY target/*-SNAPSHOT.jar app.jar

# Run as non-root
USER spring:spring

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]