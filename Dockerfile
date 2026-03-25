# ── Stage 1: Build ──────────────────────────────────────────────────────
FROM eclipse-temurin:17-jdk AS build
WORKDIR /app

# Copy Maven wrapper & pom first (cache dependencies)
COPY .mvn/ .mvn/
COPY mvnw pom.xml ./
RUN chmod +x mvnw && ./mvnw dependency:go-offline -B

# Copy source and build
COPY src/ src/
RUN ./mvnw package -DskipTests -B

# ── Stage 2: Runtime ───────────────────────────────────────────────────
FROM eclipse-temurin:17-jre
WORKDIR /app

# Copy the fat JAR from the build stage
COPY --from=build /app/target/*.jar app.jar

# Expose port (default 8080, overridable via PORT env)
EXPOSE 8080

# Run the application
ENTRYPOINT ["java", "-jar", "app.jar"]

