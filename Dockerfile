# Build stage
FROM gradle:8.1.1-jdk17 AS build
# Build stage
COPY --chown=gradle:gradle . /home/gradle/src
WORKDIR /home/gradle/src
RUN gradle clean build -x test --no-daemon

# Production stage
FROM eclipse-temurin:17-jre-alpine
EXPOSE 8002
RUN mkdir /app
RUN apk update && apk add curl jq
COPY --from=build /home/gradle/src/build/libs/*-SNAPSHOT.jar /app/app.jar
COPY sh/wait-for-postgres.sh /app/wait-for-postgres.sh
RUN ["chmod", "+x", "/app/wait-for-postgres.sh"]

CMD ["java", "-jar", "/app/app.jar"]
