package com.example.vpn.core;

import com.example.shared.IpHeader;
import com.example.shared.PacketParser;

public class SessionManager {

    private static final String TAG = "SessionManager";

    private final NatTable natTable = new NatTable();

    /**
     * Отслеживает исходящий пакет — создаёт или обновляет NAT-сессию.
     */
    public void track(IpHeader ipHeader) {
        String key = ipHeader.getNatKey();
        Session session = natTable.get(key);
        if (session == null) {
            session = new Session(
                    ipHeader.getSrcIp(), ipHeader.getSrcPort(),
                    ipHeader.getDstIp(), ipHeader.getDstPort(),
                    ipHeader.protocol
            );
            natTable.put(session);
        }
        session.touch();
    }

    /**
     * Переписывает входящий пакет: изменяет dst обратно на IP устройства.
     * @return переписанный пакет или null если сессия не найдена
     */
    public byte[] rewriteIncoming(byte[] rawPacket) {
        IpHeader hdr = PacketParser.parseIpHeader(rawPacket);
        if (hdr == null) return null;

        // В продакшне здесь: найти сессию по обратному ключу и переписать dst
        // Для MVP: возвращаем пакет без изменений
        return rawPacket;
    }

    public NatTable getNatTable() {
        return natTable;
    }
}