package com.example.server;

import com.example.crypto.SslContextProvider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;
import javax.net.ssl.SSLSocket;
import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * TLS VPN-сервер.
 * Принимает входящие соединения от Android-клиентов,
 * для каждого запускает ClientHandler.
 */
public class VpnServer {

    private static final Logger log = LoggerFactory.getLogger(VpnServer.class);

    private final int port;
    private SSLServerSocket serverSocket;
    private final ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    public VpnServer(int port) {
        this.port = port;
        this.executor = Executors.newCachedThreadPool(r -> {
            Thread t = new Thread(r, "client-handler");
            t.setDaemon(true);
            return t;
        });
    }

    public void start() throws Exception {
        SSLContext sslContext = SslContextProvider.build();
        SSLServerSocketFactory factory = sslContext.getServerSocketFactory();

        serverSocket = (SSLServerSocket) factory.createServerSocket(port);
        serverSocket.setEnabledProtocols(new String[]{"TLSv1.3"});
        serverSocket.setNeedClientAuth(false);

        running.set(true);
        log.info("VPN Server listening on :{}", port);

        // Accept loop
        while (running.get()) {
            try {
                SSLSocket clientSocket = (SSLSocket) serverSocket.accept();
                log.info("New client: {}", clientSocket.getRemoteSocketAddress());
                ClientHandler handler = new ClientHandler(clientSocket);
                executor.submit(handler);
            } catch (IOException e) {
                if (running.get()) log.error("Accept error", e);
            }
        }
    }

    public void stop() {
        running.set(false);
        executor.shutdownNow();
        try {
            if (serverSocket != null) serverSocket.close();
        } catch (IOException ignored) {
        }
    }
}