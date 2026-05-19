package com.example.vpn.vpn;

import android.util.Log;
import com.example.shared.TunnelPacket;
import com.example.vpn.core.SessionManager;
import com.example.vpn.tunnel.TunnelClient;

import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Поток: Получает ответы от VPN-сервера → записывает в TUN.
 * <p>
 * Схема:
 * TunnelClient.receive() ──► TunnelPacket.unwrap()
 * ──► SessionManager.rewrite() (правим адреса назад)
 * ──► TunInterface.writePacket()
 */
public class PacketWriter implements Runnable {

    private static final String TAG = "PacketWriter";

    private final TunInterface tunInterface;
    private final TunnelClient tunnelClient;
    private final SessionManager sessionManager;
    private final AtomicBoolean running = new AtomicBoolean(true);

    public PacketWriter(TunInterface tunInterface,
                        TunnelClient tunnelClient,
                        SessionManager sessionManager) {
        this.tunInterface = tunInterface;
        this.tunnelClient = tunnelClient;
        this.sessionManager = sessionManager;
    }

    @Override
    public void run() {
        Log.i(TAG, "PacketWriter started");
        while (running.get()) {
            try {
                // Блокирующее получение пакета от сервера
                TunnelPacket tunnelPacket = tunnelClient.receive();
                if (tunnelPacket == null) continue;

                // Извлекаем payload (оригинальный IP-пакет)
                byte[] rawPacket = tunnelPacket.unwrap();

                // NAT: переписываем destination обратно на локальный IP устройства
                byte[] rewritten = sessionManager.rewriteIncoming(rawPacket);
                if (rewritten == null) continue;

                // Пишем в TUN — ОС доставит пакет нужному приложению
                tunInterface.writePacket(rewritten);

            } catch (Exception e) {
                if (running.get()) {
                    Log.e(TAG, "Error writing packet", e);
                }
            }
        }
        Log.i(TAG, "PacketWriter stopped");
    }

    public void stop() {
        running.set(false);
    }
}