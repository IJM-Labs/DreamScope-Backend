# ── Stage 1: Build ──────────────────────────────
FROM maven:3.9.14-eclipse-temurin-25 AS build

WORKDIR /app

# Cache Maven dependencies før koden kopieres
COPY pom.xml .
RUN mvn dependency:go-offline -B

# Kopier kildekode og byg uden tests
COPY src ./src
RUN mvn package -DskipTests -B

# ── Stage 2: Runtime ─────────────────────────────
FROM eclipse-temurin:25-jre-alpine AS runtime

WORKDIR /app

# Kopier kun den færdige JAR fra build stage
COPY --from=build /app/target/*.jar app.jar

# Kør applikationen
ENTRYPOINT ["java", "-jar", "app.jar"]
