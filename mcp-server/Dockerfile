# ── Fase 1: compilación multi-módulo ──────────────────────────────────────
# Build context = raíz del repo (lo gobierna docker-compose.yml).
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
COPY mcp-server/pom.xml mcp-server/
RUN mvn -f pom.xml dependency:go-offline -pl mcp-server -am -q
COPY mcp-server/src mcp-server/src
RUN mvn -f pom.xml package -pl mcp-server -am -DskipTests -q

# ── Fase 2: imagen final ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/mcp-server/target/*.jar app.jar
EXPOSE 8081
ENTRYPOINT ["java", "-jar", "app.jar"]
