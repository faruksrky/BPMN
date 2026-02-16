FROM eclipse-temurin:17-jre-alpine
WORKDIR /app

# JAR'ı build et (multi-stage için maven gerekir - basit: önceden build edilmiş jar kopyala)
# Build: ./mvnw package -DskipTests
COPY target/*.jar app.jar

EXPOSE 8082
ENTRYPOINT ["java", "-jar", "app.jar"]
