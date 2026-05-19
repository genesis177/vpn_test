package com.example;

import com.example.server.VpnServer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Точка входа VPN-сервера.
 * Запуск: java -jar vpn-server.jar [port]
 * По умолчанию порт 8443.
 */
public class ServerLauncher {

    private static final Logger log = LoggerFactory.getLogger(ServerLauncher.class);
    private static final int DEFAULT_PORT = 8443;

    public static void main(String[] args) throws Exception {
        int port = args.length > 0 ? Integer.parseInt(args[0]) : DEFAULT_PORT;

        log.info("Starting TUN VPN Server on port {}", port);
        log.info("TLS 1.3 | TCP relay mode");

        VpnServer server = new VpnServer(port);

        // Graceful shutdown
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            log.info("Shutting down...");
            server.stop();
        }));

        server.start();  // blocking
    }
}

