# ==ETAPA 1: Construcción del JAR con Maven==
FROM maven:3.9.9-eclipse-temurin-21 AS build
WORKDIR /app
COPY pom.xml ./
RUN mvn dependency:go-offline -B
COPY src ./src
RUN mvn clean package -DskipTests

# ==ETAPA 2: Configuración de la app Java ==
FROM eclipse-temurin:21-jre-alpine
WORKDIR /app
COPY --from=build /app/target/*.jar app.jar
EXPOSE 8080
CMD ["java", "-jar", "app.jar"]


# ------------------------------------- COMANDOS ----------------------------------------------------------
# Construir la imagen, ATENCION!!! existe un punto al final que se debe incluir
#> docker build -t devops .

# Crea y arrancar el contenedor a partir de la imagen
#> docker run -d --name devops-app  -p 8080:8080 devops

# Arranca el contenedor
#> docker start devops-app
