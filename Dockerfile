# 1. Build stage – same as you already had
FROM maven:3.9-eclipse-temurin-17 AS build
WORKDIR /app

COPY pom.xml .
COPY src ./src

RUN mvn -B -DskipTests clean package

# 2. Runtime stage – plain JDK (no JavaFX/X11 needed)
FROM eclipse-temurin:17-jdk
WORKDIR /app

COPY --from=build /app/target/*.jar /app/app.jar

# Optional: default DB config (can be overridden at runtime)
ENV DB_HOST=mariadb \
    DB_PORT=3306 \
    DB_NAME=shopping_cart_db \
    DB_USER=root \
    DB_PASSWORD=example

# Run the headless demo main class (NOT the JavaFX Application)
CMD ["java", "-cp", "/app/app.jar", "otp2.shoppingcartapp.dockerimage.HeadlessCartDemo"]
