package com.example.shared;

/**
 * Туннельный фрейм на стороне сервера.
 * Содержит только payload (IP-пакет).
 */
public class TunnelFrame {

    private final byte[] payload;

    public TunnelFrame(byte[] payload) {
        this.payload = payload;
    }

    public byte[] getPayload() {
        return payload;
    }

    public int getLength() {
        return payload.length;
    }
}