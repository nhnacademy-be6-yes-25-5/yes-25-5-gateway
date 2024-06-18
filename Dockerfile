# Dockerfile
# 로컬에서 '~/.ssh/id_rsa.pub' (공개키) = 원격에서 '~/.ssh/authorized_keys' (인증키)
# 로컬에서 '~/.ssh/id_rsa' (개인키)를 SSH_PRIVATE_KEY에 삽입

# Use the official Maven image with Java 11
FROM maven:3.8.8-eclipse-temurin-21

# Set the working directory
# 이 부분 사용자의 디렉토리에 맞게 수정
WORKDIR /gateway

# Copy the pom.xml and download dependencies
COPY pom.xml .
RUN mvn dependency:go-offline

# Copy the rest of the application code
COPY . .

# Build the application
RUN mvn package

# Default command
# 이 부분도 jar파일 생성위치에 맞게 수정
CMD ["java", "-jar", "target/api-gateway-0.0.1-SNAPSHOT.jar"]
