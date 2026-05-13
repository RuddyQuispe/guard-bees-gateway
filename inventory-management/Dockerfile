FROM amazoncorretto:25-alpine-jdk

WORKDIR /app
COPY /target/inventory-management-0.0.1.jar /app

ENV TZ="America/La_Paz"

EXPOSE 8080

CMD ["java", "-jar", "inventory-management-0.0.1.jar", "-Xms128m -Xmx128m"]