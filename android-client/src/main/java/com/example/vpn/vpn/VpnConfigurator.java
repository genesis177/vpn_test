package com.example.vpn.vpn;

import android.net.VpnService;
import android.os.ParcelFileDescriptor;

/**
 * Настраивает TUN-интерфейс: IP, маршруты, DNS, MTU.
 * Вызывается один раз при старте VPN.
 */
public class VpnConfigurator {

    // Виртуальный IP устройства в VPN-сети
    private static final String VPN_IP = "10.0.0.2";
    private static final int VPN_PREFIX = 24;
    // Весь трафик через VPN
    private static final String ROUTE_IP = "0.0.0.0";
    private static final int ROUTE_PREFIX = 0;
    // DNS через Cloudflare
    private static final String DNS_SERVER = "1.1.1.1";
    private static final int MTU = 1500;

    private final VpnService service;

    public VpnConfigurator(VpnService service) {
        this.service = service;
    }

    /**
     * Создаёт и возвращает файловый дескриптор TUN-интерфейса.
     * Через этот дескриптор читаются/пишутся IP-пакеты устройства.
     */
    public ParcelFileDescriptor configure() {
        VpnService.Builder builder = service.new Builder();
        builder.setMtu(MTU)
                .addAddress(VPN_IP, VPN_PREFIX)
                .addRoute(ROUTE_IP, ROUTE_PREFIX)
                .addDnsServer(DNS_SERVER)
                .setSession("TunVPN")
                .setBlocking(true);        // блокирующее чтение для простоты
        return builder.establish();
    }
}