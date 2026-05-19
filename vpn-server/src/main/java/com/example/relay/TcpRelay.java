package com.example.relay;


import com.example.shared.IpHeader;
import com.example.shared.PacketParser;
import com.example.shared.TunnelEncoder;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class TcpRelay {

    private final TcpSession session;
    private final TunnelEncoder encoder;

    private Socket remoteSocket;
    private volatile boolean active = true;

    public TcpRelay(IpHeader ipHeader,
                    TunnelEncoder encoder,
                    SessionStore sessionStore) {

        this.encoder = encoder;

        this.session = new TcpSession(
                ipHeader.getSrcIp(),
                ipHeader.getSrcPort(),
                ipHeader.getDstIp(),
                ipHeader.getDstPort()
        );

        try {
            remoteSocket = new Socket(ipHeader.getDstIp(), ipHeader.getDstPort());

            Thread reader = new Thread(this::readRemoteLoop);
            reader.start();

        } catch (Exception e) {
            close();
        }
    }

    public void handlePacket(byte[] packet) {
        try {
            if (!active) return;

            IpHeader ip = PacketParser.parseIpHeader(packet);
            if (ip == null) return;

            int payloadOffset = ip.headerLength + 20;
            int payloadLength = packet.length - payloadOffset;

            if (payloadLength <= 0) return;

            OutputStream out = remoteSocket.getOutputStream();
            out.write(packet, payloadOffset, payloadLength);
            out.flush();

        } catch (Exception e) {
            close();
        }
    }

    private void readRemoteLoop() {
        try {
            InputStream in = remoteSocket.getInputStream();
            byte[] buf = new byte[8192];

            while (active) {
                int len = in.read(buf);
                if (len < 0) break;

                byte[] response = new byte[len];
                System.arraycopy(buf, 0, response, 0, len);

                encoder.writeFrame(response);
            }

        } catch (Exception ignored) {
        } finally {
            close();
        }
    }

    public boolean isActive() {
        return active;
    }

    public TcpSession getSession() {
        return session;
    }

    public void close() {
        active = false;

        try {
            if (remoteSocket != null) remoteSocket.close();
        } catch (Exception ignored) {
        }
    }
}