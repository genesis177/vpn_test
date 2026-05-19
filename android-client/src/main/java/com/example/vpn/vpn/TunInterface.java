package com.example.vpn.vpn;

import android.os.ParcelFileDescriptor;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * Обёртка над TUN-интерфейсом.
 * Предоставляет read()/write() для IP-пакетов.
 * <p>
 * Каждый вызов read() возвращает ровно один IP-пакет.
 * Каждый вызов write() помещает один IP-пакет обратно в стек ОС.
 */
public class TunInterface {

    private static final int MAX_PACKET_SIZE = 65535;

    private final ParcelFileDescriptor pfd;
    private final FileInputStream in;
    private final FileOutputStream out;

    public TunInterface(ParcelFileDescriptor pfd) throws IOException {
        this.pfd = pfd;
        this.in = new FileInputStream(pfd.getFileDescriptor());
        this.out = new FileOutputStream(pfd.getFileDescriptor());
    }

    /**
     * Читает один IP-пакет из TUN.
     *
     * @return байты пакета или null при ошибке
     */
    public byte[] readPacket() throws IOException {
        byte[] buf = new byte[MAX_PACKET_SIZE];
        int len = in.read(buf);
        if (len < 0) return null;
        byte[] packet = new byte[len];
        System.arraycopy(buf, 0, packet, 0, len);
        return packet;
    }

    /**
     * Записывает один IP-пакет в TUN (возвращает приложениям).
     */
    public void writePacket(byte[] data) throws IOException {
        out.write(data);
    }

    public void close() throws IOException {
        in.close();
        out.close();
        pfd.close();
    }
}