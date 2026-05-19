package com.example.shared;

import java.io.DataInputStream;
import java.io.EOFException;
import java.io.IOException;

/**
 * Читает TunnelFrame из TLS-потока.
 * <p>
 * Протокол:
 * [int: 4 байта — длина payload] [byte[]: payload]
 */
public class TunnelDecoder {

    private static final int MAX_PAYLOAD = 65535;

    private final DataInputStream in;

    public TunnelDecoder(DataInputStream in) {
        this.in = in;
    }

    /**
     * Блокирует до получения следующего фрейма.
     *
     * @return TunnelFrame или null если поток завершён
     * @throws EOFException если соединение закрыто чисто
     */
    public TunnelFrame readFrame() throws IOException {
        int length;
        try {
            length = in.readInt();
        } catch (EOFException e) {
            return null;
        }

        if (length <= 0 || length > MAX_PAYLOAD) {
            throw new IOException("Invalid frame length: " + length);
        }

        byte[] payload = new byte[length];
        in.readFully(payload);
        return new TunnelFrame(payload);
    }
}
