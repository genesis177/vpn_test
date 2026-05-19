package com.example.crypto;

import javax.net.ssl.*;
import java.io.FileInputStream;
import java.security.KeyStore;

/**
 * Загружает TLS-контекст для сервера.
 * <p>
 * Требует JKS keystore с self-signed сертификатом.
 * <p>
 * Генерация сертификата (выполнить один раз):
 * keytool -genkeypair -alias vpnserver \
 * -keyalg RSA -keysize 2048 \
 * -validity 3650 \
 * -keystore keystore.jks \
 * -storepass changeit \
 * -dname "CN=vpnserver, O=TunVPN, C=RU"
 * <p>
 * Положить keystore.jks рядом с JAR или указать путь в KEYSTORE_PATH.
 */
public class SslContextProvider {

    private static final String KEYSTORE_PATH = System.getenv()
            .getOrDefault("KEYSTORE_PATH", "keystore.jks");
    private static final String KEYSTORE_PASSWORD = System.getenv()
            .getOrDefault("KEYSTORE_PASSWORD", "password");

    public static SSLContext build() throws Exception {
        // Загружаем keystore
        KeyStore keyStore = KeyStore.getInstance("JKS");
        try (FileInputStream fis = new FileInputStream(KEYSTORE_PATH)) {
            keyStore.load(fis, KEYSTORE_PASSWORD.toCharArray());
        }

        // KeyManager — приватный ключ сервера
        KeyManagerFactory kmf = KeyManagerFactory.getInstance(
                KeyManagerFactory.getDefaultAlgorithm());
        kmf.init(keyStore, KEYSTORE_PASSWORD.toCharArray());

        // TrustManager — для проверки клиентов (не требуется в нашем случае)
        TrustManagerFactory tmf = TrustManagerFactory.getInstance(
                TrustManagerFactory.getDefaultAlgorithm());
        tmf.init(keyStore);

        SSLContext sslContext = SSLContext.getInstance("TLS");
        sslContext.init(kmf.getKeyManagers(), tmf.getTrustManagers(), null);

        return sslContext;
    }
}