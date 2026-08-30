FROM eclipse-temurin:21-jdk AS builder

WORKDIR /app

# Copy Maven wrapper and Maven configuration first
COPY .mvn .mvn
COPY mvnw .
COPY pom.xml .

# Make Maven wrapper executable and download dependencies
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy application source
COPY src src

# Build executable JAR
RUN ./mvnw clean package -DskipTests


FROM eclipse-temurin:21-jre

WORKDIR /app

# Copy only the built JAR from the builder stage
COPY --from=builder /app/target/*.jar app.jar

EXPOSE 9090

ENTRYPOINT ["java", "-jar", "app.jar"]