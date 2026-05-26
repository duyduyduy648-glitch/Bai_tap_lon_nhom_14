# Stage 1: Build - Dùng Maven để compile và đóng gói thành JAR
FROM maven:3.9-eclipse-temurin-21 AS build
WORKDIR /app

# Copy file cấu hình Maven trước để tận dụng cache Docker
COPY pom.xml .
RUN mvn dependency:go-offline -q

# Copy toàn bộ source code và build
COPY src ./src
RUN mvn clean package -DskipTests -q

# Stage 2: Run - Dùng JRE nhỏ gọn để chạy (không cần cả bộ JDK nặng)
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app

# Copy file JAR server đã build xong từ stage 1
COPY --from=build /app/target/AuctionProject-1.0-SNAPSHOT-server.jar app.jar

# Chạy AuctionServer (PORT sẽ được Railway tự inject vào)
CMD ["java", "-jar", "app.jar"]
