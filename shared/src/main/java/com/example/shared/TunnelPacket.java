package com.example.shared;

/**
 * Фрейм туннеля: обёртка вокруг сырого IP-пакета.
 * <p>
 * Формат на проводе:
 * [int: 4 байта — длина] [byte[]: payload]
 * <p>
 * TunnelPacket хранит только payload (без длины — она добавляется при отправке).
 */
public class TunnelPacket {

    private final byte[] data;

    public TunnelPacket(byte[] data) {
        this.data = data;
    }

    /**
     * Оборачивает сырой IP-пакет в TunnelPacket
     */
    public static TunnelPacket wrap(byte[] rawIpPacket) {
        return new TunnelPacket(rawIpPacket);
    }

    /**
     * Извлекает сырой IP-пакет
     */
    public byte[] unwrap() {
        return data;
    }

    public byte[] getData() {
        return data;
    }

    public int getLength() {
        return data.length;
    }
}
