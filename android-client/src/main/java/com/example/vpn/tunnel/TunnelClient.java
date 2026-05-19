package com.example.vpn.tunnel;

import com.example.shared.TunnelPacket;
import com.example.vpn.vpn.MyVpnService;

import javax.net.ssl.*;
import java.io.*;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

public class TunnelClient {

    private static final String TAG = "TunnelClient";

    private final String host;
    private final int port;
    private final VpnService vpnService;

    private SSLSocket socket;
    private DataInputStream in;
    private DataOutputStream out;

    public TunnelClient(String host, int port, MyVpnService vpnService) {
        this.host = host;
        this.port = port;
        this.vpnService = vpnService;
    }

    public synchronized void connect() throws Exception {

        TrustManager[] trustAll = new TrustManager[]{
                new X509TrustManager() {
                    @Override
                    public void checkClientTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public void checkServerTrusted(X509Certificate[] chain, String authType) {
                    }

                    @Override
                    public X509Certificate[] getAcceptedIssuers() {
                        return new X509Certificate[0];
                    }
                }
        };

        SSLContext context = SSLContext.getInstance("TLS");
        context.init(null, trustAll, new SecureRandom());

        SSLSocketFactory factory = context.getSocketFactory();

        socket = (SSLSocket) factory.createSocket(host, port);

        vpnService.protect(socket);

        socket.startHandshake();

        in = new DataInputStream(new BufferedInputStream(socket.getInputStream()));
        out = new DataOutputStream(new BufferedOutputStream(socket.getOutputStream()));

        Log.i(TAG, "TLS connected");
    }

    public synchronized void send(TunnelPacket packet) throws IOException {
        byte[] data = packet.getData();

        out.writeInt(data.length);
        out.write(data);
        out.flush();
    }

    public synchronized TunnelPacket receive() throws IOException {
        int length = in.readInt();

        if (length <= 0 || length > 65535) {
            throw new IOException("Invalid packet size");
        }

        byte[] data = new byte[length];
        in.readFully(data);

        return new TunnelPacket(data);
    }

    public synchronized void close() {
        try {
            if (socket != null) socket.close();
        } catch (Exception ignored) {
        }
    }
}