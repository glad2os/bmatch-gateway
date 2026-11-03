# Build stage
FROM gradle:8.14.6-jdk17 AS build
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

# Production stage
FROM eclipse-temurin:17-jre-alpine
EXPOSE 8002
RUN apk upgrade --no-cache && apk add --no-cache curl jq \
    && addgroup -S app && adduser -S -G app app && mkdir /app
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar /app/app.jar
COPY --chmod=755 sh/wait-for-postgres.sh /app/wait-for-postgres.sh

USER app
HEALTHCHECK --interval=10s --timeout=3s --start-period=60s --retries=5 \
    CMD ["curl", "-fsS", "http://localhost:8002/actuator/health"]

CMD ["java", "-jar", "/app/app.jar"]
