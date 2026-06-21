# 1. Build Stage: Compile and Package the Application
FROM maven:3.8.5-openjdk-17 AS build
WORKDIR /app
COPY pom.xml .
# Download dependencies first (cache layer)
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# 2. Deploy Stage: Deploy package to Tomcat Server
FROM tomcat:10.1-jdk17-openjdk-slim
# Clean default Tomcat webapps
RUN rm -rf /usr/local/tomcat/webapps/*
# Copy war file into deployment path
COPY --from=build /app/target/quickbite.war /usr/local/tomcat/webapps/quickbite.war
EXPOSE 8080
CMD ["catalina.sh", "run"]
