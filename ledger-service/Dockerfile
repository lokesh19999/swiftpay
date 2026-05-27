FROM maven:3.9.6-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml .
COPY shared-kernel/pom.xml shared-kernel/pom.xml
COPY transaction-service/pom.xml transaction-service/pom.xml
COPY ledger-service/pom.xml ledger-service/pom.xml
COPY analytics-service/pom.xml analytics-service/pom.xml
COPY shared-kernel/src shared-kernel/src
COPY ledger-service/src ledger-service/src
RUN mvn -pl shared-kernel,ledger-service -am package -DskipTests -q

FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
RUN addgroup -S swiftpay && adduser -S swiftpay -G swiftpay
USER swiftpay
COPY --from=build /app/ledger-service/target/ledger-service-*.jar app.jar
EXPOSE 8082
ENTRYPOINT ["java", "-XX:+UseContainerSupport", "-XX:MaxRAMPercentage=75", "-jar", "app.jar"]
