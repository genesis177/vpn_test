package com.example.shared;

import java.io.DataOutputStream;
import java.io.IOException;

/**
 * Пишет TunnelFrame в TLS-поток (сервер → клиент).
 * <p>
 * Протокол:
 * [int: 4 байта — длина] [byte[]: payload]
 * <p>
 * Синхронизирован: к encoder могут обращаться несколько TcpRelay потоков.
 */
public class TunnelEncoder {

    private final DataOutputStream out;

    public TunnelEncoder(DataOutputStream out) {
        this.out = out;
    }

    public synchronized void writeFrame(byte[] payload) throws IOException {
        out.writeInt(payload.length);
        out.write(payload);
        out.flush();
    }

    public synchronized void writeFrame(TunnelFrame frame) throws IOException {
        writeFrame(frame.getPayload());
    }
}