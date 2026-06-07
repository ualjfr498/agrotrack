# ── Fase 1: compilación multi-módulo ──────────────────────────────────────
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
# Parent pom + pom de AMBOS módulos. El parent declara backend y mcp-server,
# así que Maven necesita los dos pom para construir el modelo del reactor,
# aunque -pl backend solo compile el backend (el pom hermano es barato y no
# arrastra su código). Sin el pom de mcp-server, Maven aborta el reactor.
COPY pom.xml ./
COPY backend/pom.xml backend/
COPY mcp-server/pom.xml mcp-server/
RUN mvn -f pom.xml dependency:go-offline -pl backend -am -q
COPY backend/src backend/src
RUN mvn -f pom.xml package -pl backend -am -DskipTests -q

# ── Fase 2: imagen final ───────────────────────────────────────────────────
FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=build /app/backend/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]
