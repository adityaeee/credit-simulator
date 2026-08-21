# Stage Build
FROM maven:3.9.6-eclipse-temurin-17 AS build

WORKDIR /app

# Copy pom.xml
RUN mvn dependency:go-offline -B

# Copy src
COPY src ./src
RUN mvn clean package -DskipTests

# Stage Run
FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# ambil cuman hasil jar dari stage build, engga seluruh project
COPY --from=build /app/target/credit-simulator.jar ./credit-simulator.jar

# saample file input, biar bisa langsung dicoba engga perlu volume mount
COPY file_inputs.txt ./file_inputs.txt

# ENTRYPOINT
ENTRYPOINT ["java", "-jar", "credit-simulator.jar"]