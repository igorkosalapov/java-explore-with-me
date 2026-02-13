FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY . .
RUN mvn -q -DskipTests -pl ewm-service/ewm-server -am clean package

FROM eclipse-temurin:21-jre
WORKDIR /app
COPY --from=build /app/ewm-service/ewm-server/target/*.jar app.jar
ENTRYPOINT ["java","-jar","app.jar"]
