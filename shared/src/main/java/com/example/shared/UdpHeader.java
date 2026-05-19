package com.example.shared;

/**
 * UDP заголовок (RFC 768).
 * <p>
 * 0      7 8     15 16    23 24    31
 * +--------+--------+--------+--------+
 * |     Source      |   Destination   |
 * |      Port       |      Port       |
 * +--------+--------+--------+--------+
 * |                 |                 |
 * |     Length      |    Checksum     |
 * +--------+--------+--------+--------+
 */
public class UdpHeader {

    public final int srcPort;
    public final int dstPort;
    public final int length;
    public final int checksum;

    private UdpHeader(int srcPort, int dstPort, int length, int checksum) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.length = length;
        this.checksum = checksum;
    }

    public static UdpHeader parse(byte[] raw, int offset) {
        int srcPort = ((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF);
        int dstPort = ((raw[offset + 2] & 0xFF) << 8) | (raw[offset + 3] & 0xFF);
        int length = ((raw[offset + 4] & 0xFF) << 8) | (raw[offset + 5] & 0xFF);
        int checksum = ((raw[offset + 6] & 0xFF) << 8) | (raw[offset + 7] & 0xFF);
        return new UdpHeader(srcPort, dstPort, length, checksum);
    }

    public int getDataOffset() {
        return 8;
    }
}
