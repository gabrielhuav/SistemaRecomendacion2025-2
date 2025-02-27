FROM maven:3.9-eclipse-temurin-21 AS builder

WORKDIR /app

# Copiamos el archivo pom.xml y descargamos las dependencias
COPY pom.xml /app/

# Descargar dependencias
RUN mvn dependency:go-offline

# Copiamos el código fuente
COPY src /app/src

# Compilamos la aplicación
RUN mvn package -DskipTests

# Segunda etapa: imagen de ejecución más pequeña
FROM eclipse-temurin:21-jre

WORKDIR /app

# Copiamos el JAR compilado de la etapa anterior
COPY --from=builder /app/target/Recomendaciones-0.0.1-SNAPSHOT.jar /app/app.jar

# Puerto que expone la aplicación
EXPOSE 8080

# Comando para ejecutar la aplicación
CMD ["java", "-jar", "app.jar"]