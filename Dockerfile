# Multi-stage Dockerfile for Enterprise Financial Core API
# Stage 1: Build Application
FROM eclipse-temurin:21-jdk-alpine AS builder

WORKDIR /app

# Copy Maven Wrapper and POM first to leverage Docker layer caching
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source code and package application
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Stage 2: Lightweight Production Runtime
FROM eclipse-temurin:21-jre-alpine AS runner

WORKDIR /app

# Run as non-privileged user for enterprise security compliance
RUN addgroup -S appgroup && adduser -S appuser -G appgroup

COPY --from=builder /app/target/financial-core-api-1.0.0.jar app.jar

USER appuser

EXPOSE 8080

ENV JAVA_OPTS="-XX:+UseG1GC -XX:MaxRAMPercentage=75.0 -Djava.security.egd=file:/dev/./urandom"

ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar app.jar"]
