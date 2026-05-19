package com.example.shared;

import java.net.InetAddress;
import java.net.UnknownHostException;

/**
 * IPv4 заголовок — серверная копия
 */
public class IpHeader {

    public static final int PROTOCOL_TCP = 6;
    public static final int PROTOCOL_UDP = 17;

    private final byte[] raw;

    public final int version;
    public final int headerLength;
    public final int totalLength;
    public final int protocol;
    public final byte[] srcAddr;
    public final byte[] dstAddr;

    public IpHeader(byte[] raw, int version, int headerLength, int totalLength,
                    int protocol, byte[] srcAddr, byte[] dstAddr) {
        this.raw = raw;
        this.version = version;
        this.headerLength = headerLength;
        this.totalLength = totalLength;
        this.protocol = protocol;
        this.srcAddr = srcAddr;
        this.dstAddr = dstAddr;
    }

    public boolean isTcp() {
        return protocol == PROTOCOL_TCP;
    }

    public boolean isUdp() {
        return protocol == PROTOCOL_UDP;
    }

    public int getSrcPort() {
        return read16(raw, headerLength);
    }

    public int getDstPort() {
        return read16(raw, headerLength + 2);
    }

    public String getSrcIp() {
        try {
            return InetAddress.getByAddress(srcAddr).getHostAddress();
        } catch (UnknownHostException e) {
            return "?";
        }
    }

    public String getDstIp() {
        try {
            return InetAddress.getByAddress(dstAddr).getHostAddress();
        } catch (UnknownHostException e) {
            return "?";
        }
    }

    public String getNatKey() {
        return getSrcIp() + ":" + getSrcPort() + "-" + getDstIp() + ":" + getDstPort();
    }

    public byte[] getRaw() {
        return raw;
    }

    private static int read16(byte[] buf, int offset) {
        if (offset + 2 > buf.length) return 0;
        return ((buf[offset] & 0xFF) << 8) | (buf[offset + 1] & 0xFF);
    }

    @Override
    public String toString() {
        return String.format("IPv%d %s:%d → %s:%d proto=%d len=%d",
                version, getSrcIp(), getSrcPort(), getDstIp(), getDstPort(),
                protocol, totalLength);
    }
}