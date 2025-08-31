# ---------- build stage ----------
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY src ./src
# If integration tests depend on external services, skip tests
RUN mvn -q clean package -DskipTests

# ---------- run stage ----------
FROM eclipse-temurin:21-jre
WORKDIR /app


COPY --from=build /app/target/tsb-banking-0.0.1-SNAPSHOT.jar app.jar

ENV TZ=Pacific/Auckland

EXPOSE 8080

# Important: enable dev profile, using the RDS connection hardcoded in application.yml
ENTRYPOINT ["java","-jar","/app/app.jar","--spring.profiles.active=dev"]
