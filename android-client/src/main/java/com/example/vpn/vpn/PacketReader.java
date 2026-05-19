package com.example.vpn.vpn;


import com.example.shared.IpHeader;
import com.example.shared.PacketParser;
import com.example.shared.TunnelPacket;
import com.example.vpn.core.SessionManager;
import com.example.vpn.tunnel.TunnelClient;


import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Поток: Читает IP-пакеты из TUN → парсит → отправляет на VPN-сервер.
 * <p>
 * Схема:
 * TUN fd ──► readPacket() ──► PacketParser ──► SessionManager.track()
 * ──► TunnelPacket.wrap() ──► TunnelClient.send()
 */
public class PacketReader implements Runnable {

    private static final String TAG = "PacketReader";

    private final TunInterface tunInterface;
    private final TunnelClient tunnelClient;
    private final SessionManager sessionManager;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public PacketReader(TunInterface tunInterface,
                        TunnelClient tunnelClient,
                        SessionManager sessionManager) {
        this.tunInterface = tunInterface;
        this.tunnelClient = tunnelClient;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        Log.i(TAG, "PacketReader started");
        while (running.get()) {
            try {
                byte[] rawPacket = tunInterface.readPacket();
                if (rawPacket == null) continue;

                // Парсим IP-заголовок
                IpHeader ipHeader = PacketParser.parseIpHeader(rawPacket);
                if (ipHeader == null) continue;

                // Только IPv4 TCP/UDP
                if (!ipHeader.isTcpOrUdp()) continue;

                // Регистрируем или обновляем сессию в NAT-таблице
                sessionManager.track(ipHeader);

                // Оборачиваем в туннельный фрейм и отправляем
                TunnelPacket tunnelPacket = TunnelPacket.wrap(rawPacket);
                tunnelClient.send(tunnelPacket);

            } catch (Exception e) {
                if (running.get()) {
                    Log.e(TAG, "Error reading packet", e);
                }
            }
        }
        Log.i(TAG, "PacketReader stopped");
    }

    public void stop() {
        running.set(false);
    }
}