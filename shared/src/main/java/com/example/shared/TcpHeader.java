package com.example.shared;

/**
 * TCP заголовок (RFC 793).
 * <p>
 * 0               1               2               3
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |          Source Port          |       Destination Port        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                        Sequence Number                        |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |                    Acknowledgment Number                      |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 * |  Data |           |U|A|P|R|S|F|                               |
 * | Offset| Reserved  |R|C|S|S|Y|I|            Window             |
 * |       |           |G|K|H|T|N|N|                               |
 * +-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+-+
 */
public class TcpHeader {

    public final int srcPort;
    public final int dstPort;
    public final long seqNumber;
    public final long ackNumber;
    public final int dataOffset;    // в байтах
    public final int flags;         // нижние 9 бит
    public final int windowSize;

    // Флаги
    public static final int FLAG_FIN = 0x001;
    public static final int FLAG_SYN = 0x002;
    public static final int FLAG_RST = 0x004;
    public static final int FLAG_PSH = 0x008;
    public static final int FLAG_ACK = 0x010;
    public static final int FLAG_URG = 0x020;

    private TcpHeader(int srcPort, int dstPort, long seqNumber, long ackNumber,
                      int dataOffset, int flags, int windowSize) {
        this.srcPort = srcPort;
        this.dstPort = dstPort;
        this.seqNumber = seqNumber;
        this.ackNumber = ackNumber;
        this.dataOffset = dataOffset;
        this.flags = flags;
        this.windowSize = windowSize;
    }

    public static TcpHeader parse(byte[] raw, int offset) {
        int srcPort = ((raw[offset] & 0xFF) << 8) | (raw[offset + 1] & 0xFF);
        int dstPort = ((raw[offset + 2] & 0xFF) << 8) | (raw[offset + 3] & 0xFF);
        long seqNum = readUint32(raw, offset + 4);
        long ackNum = readUint32(raw, offset + 8);
        int dataOffset = ((raw[offset + 12] & 0xF0) >> 4) * 4;
        int flags = ((raw[offset + 12] & 0x01) << 8) | (raw[offset + 13] & 0xFF);
        int window = ((raw[offset + 14] & 0xFF) << 8) | (raw[offset + 15] & 0xFF);
        return new TcpHeader(srcPort, dstPort, seqNum, ackNum, dataOffset, flags, window);
    }

    public boolean isSyn() {
        return (flags & FLAG_SYN) != 0;
    }

    public boolean isAck() {
        return (flags & FLAG_ACK) != 0;
    }

    public boolean isFin() {
        return (flags & FLAG_FIN) != 0;
    }

    public boolean isRst() {
        return (flags & FLAG_RST) != 0;
    }

    private static long readUint32(byte[] buf, int offset) {
        return ((buf[offset] & 0xFFL) << 24)
                | ((buf[offset + 1] & 0xFFL) << 16)
                | ((buf[offset + 2] & 0xFFL) << 8)
                | (buf[offset + 3] & 0xFFL);
    }
}