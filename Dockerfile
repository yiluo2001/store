# eclipse-temurin is one of the many java distributors
# AS build give the build alias build
FROM eclipse-temurin:26-jdk AS build
WORKDIR /app

# maven wrapper file allow using maven without installing ii on machine
# copy the maven wrapper files from your folder to the container
# .mvn and ./ are the destination inside /app
COPY .mvn ./.mvn
COPY mvnw pom.xml ./

# download the dependencies using the wrapper
# offline in a sense that working without an internet connection
RUN ./mvnw dependency:go-offline

# Copy your source code and build the package
COPY src ./src
# clean delete target folder before building
# package everything as jar file inside target folder
RUN ./mvnw clean package -DskipTests

# this stage we only need jre bc the code is already built and compiled
FROM eclipse-temurin:26-jre
WORKDIR /app
# --from=build, not from local storage, but from previous build stage
# app.jar rename the jar file in target into app.jar
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
# just java -jar app.jar
# java means jvm
# -jar tells Java to execute a packaged .jar file
# app.jar the jar file
ENTRYPOINT ["java", "-jar", "app.jar"]