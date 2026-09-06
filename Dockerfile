# syntax=docker/dockerfile:1

FROM eclipse-temurin:21-jdk AS builder
WORKDIR /app

COPY gradlew gradlew
COPY gradle gradle
COPY build.gradle settings.gradle ./
RUN chmod +x gradlew

COPY src src
RUN ./gradlew clean bootJar -x test

FROM eclipse-temurin:21-jre
WORKDIR /app

COPY --from=builder /app/build/libs/*.jar app.jar

ENV SPRING_PROFILES_ACTIVE=prod \
    JWT_SECRET_KEY=suW1MFuErxKmhelwTpwhw6QaAoqZMRb64y81cR9rYeA \
    JWT_REFRESH_KEY=WDzqnd5bXrUvjuzzLagE0Vc5BppnVvKVv3rrie5xkmk \
    JWT_ISSUER="SAVYINT CORP" \
    JWT_TTL=3600 \
    APP_SUPER_USER=admin \
    APP_SUPER_PASSWORD=123456a@ \
    CORS_ALLOWED_ORIGIN_1=https://ob-consent.savyint.com \
    CORS_ALLOWED_ORIGIN_2=https://iam-uat.savyint.com

EXPOSE 8080
ENTRYPOINT ["java", "-jar", "/app/app.jar"]
