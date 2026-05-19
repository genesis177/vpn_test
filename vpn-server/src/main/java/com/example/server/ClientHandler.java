package com.example.server;


import com.example.relay.SessionStore;
import com.example.relay.TcpRelay;
import com.example.shared.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.net.ssl.SSLSocket;
import java.io.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * Обработчик одного VPN-клиента.
 * <p>
 * Читает TunnelFrame с пакетами от клиента → парсит IP-заголовок →
 * пробрасывает TCP-соединение на реальный сервер в интернете →
 * ответы отправляет обратно клиенту.
 */
public class ClientHandler implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(ClientHandler.class);

    private final SSLSocket socket;
    private final TunnelDecoder decoder;
    private final TunnelEncoder encoder;
    private final SessionStore sessionStore;
    private final ExecutorService relayExecutor;
    private volatile boolean active = true;

    public ClientHandler(SSLSocket socket) throws IOException {
        this.socket = socket;
        this.decoder = new TunnelDecoder(new DataInputStream(
                new BufferedInputStream(socket.getInputStream())));
        this.encoder = new TunnelEncoder(new DataOutputStream(
                new BufferedOutputStream(socket.getOutputStream())));
        this.sessionStore = new SessionStore();
        this.relayExecutor = Executors.newCachedThreadPool();
    }

    @Override
    public void run() {
        log.info("Handler started for {}", socket.getRemoteSocketAddress());
        try {
            while (active) {
                // 1. Читаем фрейм от клиента
                TunnelFrame frame = decoder.readFrame();
                if (frame == null) break;

                // 2. Парсим IP-заголовок
                IpHeader ipHeader = PacketParser.parseIpHeader(frame.getPayload());
                if (ipHeader == null) continue;

                log.debug("← {}", ipHeader);

                // 3. Только TCP в MVP (UDP и ICMP — следующий этап)
                if (!ipHeader.isTcp()) {
                    log.debug("Skipping non-TCP packet (proto={})", ipHeader.protocol);
                    continue;
                }

                // 4. Проксируем TCP через TcpRelay
                TcpRelay relay = sessionStore.getOrCreate(ipHeader, () ->
                        new TcpRelay(ipHeader, encoder, sessionStore));

                relay.handlePacket(frame.getPayload());
            }
        } catch (EOFException e) {
            log.info("Client disconnected: {}", socket.getRemoteSocketAddress());
        } catch (Exception e) {
            if (active) log.error("Handler error", e);
        } finally {
            shutdown();
        }
    }

    private void shutdown() {
        active = false;
        relayExecutor.shutdownNow();
        sessionStore.closeAll();
        try {
            socket.close();
        } catch (IOException ignored) {
        }
        log.info("Handler closed for {}", socket.getRemoteSocketAddress());
    }
}