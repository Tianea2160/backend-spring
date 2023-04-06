# build stage
FROM gradle:jdk17-alpine as build
WORKDIR /app
COPY . .
RUN ./gradlew clean build

# deploy stage
FROM openjdk:17
WORKDIR /app
COPY --from=build /app/build/libs/** /app/app.jar
ENTRYPOINT ["java","-jar","/app/app.jar"]