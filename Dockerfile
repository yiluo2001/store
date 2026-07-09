# Build stage using a verified plain Java 26 JDK image
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# Copy the maven wrapper files from your folder to the container
COPY .mvn/ .mvn
COPY mvnw pom.xml ./

# Download the dependencies using the wrapper
RUN ./mvnw dependency:go-offline

# Copy your source code and build the package
COPY src ./src
RUN ./mvnw clean package -DskipTests

# Run stage using a runtime Java 26 image
FROM eclipse-temurin:26-jre
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
ENTRYPOINT ["java", "-jar", "app.jar"]