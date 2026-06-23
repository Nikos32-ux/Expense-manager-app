FROM maven:3.9-eclipse-temurin-21-jammy AS builder
WORKDIR /app
COPY ./pom.xml .
RUN --mount=type=cache,target=/root/.m2 mvn -B dependency:resolve
COPY ./src .
RUN --mount=type=cache,target=/root/.m2 mvn package -DskipTests

FROM eclipse-temurin:21-jre-jammy
WORKDIR /app
COPY --from=builder /app/target/ExpenseTracker-0.0.1-SNAPSHOT.jar app.jar
EXPOSE 8080
ENTRYPOINT ["sh", "-c", "java $JAVA_OPTS -jar /app.jar"]

