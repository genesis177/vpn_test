package com.example.vpn.tunnel;

/**
 * Константы туннеля.
 * Измените SERVER_HOST и SERVER_PORT под ваш VPS.
 */
public final class TunnelConstants {

    private TunnelConstants() {
    }

    // ── Адрес VPN-сервера ──────────────────────────────────
    public static final String SERVER_HOST = "your-server-ip";
    public static final int SERVER_PORT = 8443;

    // ── Размеры ────────────────────────────────────────────
    public static final int MAX_PACKET_SIZE = 65535;
    public static final int FRAME_HEADER_SIZE = 4;   // int: длина payload

    // ── Keepalive ──────────────────────────────────────────
    public static final int KEEPALIVE_INTERVAL_MS = 20_000;
    public static final int RECONNECT_DELAY_MS = 3_000;

    // ── VPN-сеть ───────────────────────────────────────────
    public static final String VPN_SUBNET = "10.0.0.0";
    public static final int VPN_PREFIX = 24;
    public static final String VPN_GATEWAY = "10.0.0.1";   // IP сервера в туннеле
}
