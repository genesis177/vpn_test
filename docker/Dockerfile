FROM eclipse-temurin:17-jre-alpine

WORKDIR /app

# Копируем fat JAR (собирается через ./gradlew jar)
COPY vpn-server/build/libs/vpn-server-1.0-SNAPSHOT.jar app.jar

# Копируем keystore (генерируется один раз — см. SslContextProvider.java)
COPY keystore.jks keystore.jks

# Переменные окружения (можно переопределить в docker-compose)
ENV KEYSTORE_PATH=/app/keystore.jks
ENV KEYSTORE_PASSWORD=changeit
ENV SERVER_PORT=8443

EXPOSE 8443

ENTRYPOINT ["java", "-jar", "app.jar"]
CMD ["8443"]
