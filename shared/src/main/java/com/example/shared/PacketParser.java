package com.example.shared;

public class PacketParser {

    private PacketParser() {
    }

    public static IpHeader parseIpHeader(byte[] raw) {
        if (raw == null || raw.length < 20) return null;
        int versionIhl = raw[0] & 0xFF;
        int version = (versionIhl >> 4) & 0xF;
        if (version != 4) return null;
        int headerLength = (versionIhl & 0xF) * 4;
        if (headerLength < 20 || headerLength > raw.length) return null;
        int totalLength = ((raw[2] & 0xFF) << 8) | (raw[3] & 0xFF);
        int protocol = raw[9] & 0xFF;
        byte[] srcAddr = new byte[4];
        byte[] dstAddr = new byte[4];
        System.arraycopy(raw, 12, srcAddr, 0, 4);
        System.arraycopy(raw, 16, dstAddr, 0, 4);
        return new IpHeader(raw, version, headerLength, totalLength, protocol, srcAddr, dstAddr);
    }

    public static TcpHeader parseTcpHeader(byte[] raw, int offset) {
        if (raw.length < offset + 20) return null;
        return TcpHeader.parse(raw, offset);
    }
}