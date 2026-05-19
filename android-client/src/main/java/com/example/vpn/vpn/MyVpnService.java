package com.example.vpn.vpn;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Intent;
import android.net.VpnService;
import android.os.ParcelFileDescriptor;
import android.util.Log;
import com.example.vpn.core.SessionManager;
import com.example.vpn.tunnel.TunnelClient;
import com.example.vpn.tunnel.TunnelConstants;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Главный VPN-сервис Android.
 * 1. Создаёт TUN-интерфейс через VpnService.Builder
 * 2. Запускает PacketReader (читает IP-пакеты из TUN)
 * 3. Запускает PacketWriter (пишет ответы обратно в TUN)
 * 4. Пробрасывает пакеты через TunnelClient (TLS -> сервер)
 */
public class MyVpnService extends VpnService {

    private static final String TAG = "MyVpnService";
    public static final String ACTION_START = "com.example.vpn.START";
    public static final String ACTION_STOP = "com.example.vpn.STOP";

    private ParcelFileDescriptor tunFd;
    private TunInterface tunInterface;
    private TunnelClient tunnelClient;
    private SessionManager sessionManager;
    private PacketReader packetReader;
    private PacketWriter packetWriter;
    private ExecutorService executor;
    private final AtomicBoolean running = new AtomicBoolean(false);

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null && ACTION_STOP.equals(intent.getAction())) {
            stopVpn();
            return START_NOT_STICKY;
        }
        if (!running.get()) {
            startVpn();
        }
        return START_STICKY;
    }

    private void startVpn() {
        try {
            // 1. Настройка и поднятие TUN-интерфейса
            VpnConfigurator configurator = new VpnConfigurator(this);
            tunFd = configurator.configure();

            // 2. TUN wrapper
            tunInterface = new TunInterface(tunFd);

            // 3. TLS-туннель к серверу
            tunnelClient = new TunnelClient(
                    TunnelConstants.SERVER_HOST,
                    TunnelConstants.SERVER_PORT,
                    this
            );
            tunnelClient.connect();

            // 4. Session manager (NAT-таблица)
            sessionManager = new SessionManager();

            // 5. Reader и Writer
            packetReader = new PacketReader(tunInterface, tunnelClient, sessionManager);
            packetWriter = new PacketWriter(tunInterface, tunnelClient, sessionManager);

            executor = Executors.newFixedThreadPool(2);
            running.set(true);

            executor.submit(packetReader);
            executor.submit(packetWriter);

            startForegroundVpn();

            Log.i(TAG, "VPN started");
        } catch (Exception e) {
            Log.e(TAG, "Failed to start VPN", e);
            stopVpn();
        }
    }

    private void stopVpn() {
        running.set(false);
        if (executor != null) executor.shutdownNow();
        if (packetReader != null) packetReader.stop();
        if (packetWriter != null) packetWriter.stop();
        if (tunnelClient != null) tunnelClient.close();
        if (tunFd != null) {
            try {
                tunFd.close();
            } catch (IOException ignored) {
            }
        }
        stopSelf();
        Log.i(TAG, "VPN stopped");
    }

    @Override
    public void onRevoke() {
        stopVpn();
    }

    private void startForegroundVpn() {
        NotificationChannel channel = new NotificationChannel(
                "vpn",
                "VPN",
                NotificationManager.IMPORTANCE_LOW
        );

        NotificationManager nm = getSystemService(NotificationManager.class);
        nm.createNotificationChannel(channel);

        Notification notification = new Notification.Builder(this, "vpn")
                .setContentTitle("TunVPN")
                .setContentText("VPN active")
                .setSmallIcon(android.R.drawable.stat_sys_download_done)
                .build();

        startForeground(1, notification);
    }
}