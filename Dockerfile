FROM openjdk:17-jdk-slim as builder
COPY gradlew .
COPY gradle gradle
COPY build.gradle .
COPY settings.gradle .
COPY src src
COPY .env .env
RUN chmod +x ./gradlew
RUN ./gradlew bootjar

ENV TZ=Asia/Seoul
FROM openjdk:17-jdk-slim
COPY --from=builder build/libs/*.jar app.jar
COPY --from=builder .env .env
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-Duser.timezone=Asia/Seoul","-jar","/app.jar"]