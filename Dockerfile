# CI(.github/workflows/ci.yml)에서 ./gradlew bootJar 로 만든 jar 를 담기만 한다.
# 컴파일을 이미지 빌드 밖으로 빼서 amd64/arm64 멀티아키 이미지를 QEMU 없이 수 초에 만든다.
# (OCI Ampere A1 은 arm64, 기존 EC2 는 amd64)
FROM eclipse-temurin:17-jdk
ENV TZ=Asia/Seoul
COPY build/libs/*.jar app.jar
COPY .env .env
EXPOSE 8080
ENTRYPOINT ["java","-Dspring.profiles.active=prod","-Duser.timezone=Asia/Seoul","-jar","/app.jar"]
