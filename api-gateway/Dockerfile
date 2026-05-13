FROM amazoncorretto:25-alpine-jdk

WORKDIR /app
COPY /target/api-gateway-0.0.1-SNAPSHOT.jar /app

ENV TZ="America/La_Paz"

EXPOSE 8080

CMD ["java", "-jar", "api-gateway-0.0.1-SNAPSHOT.jar", "-Xms128m -Xmx128m"]