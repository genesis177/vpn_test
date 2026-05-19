package com.example.vpn.core;

import com.example.shared.IpHeader;
import com.example.shared.PacketParser;

/**
 * Менеджер сессий: высокоуровневый NAT.
 * <p>
 * track()            — регистрирует исходящий пакет
 * rewriteIncoming()  — переписывает адрес назначения в ответном пакете
 * обратно на локальный IP устройства
 * <p>
 * ────────────────────────────────────────────────────────────────
 * Реальный NAT требует:
 * 1. Перезаписи src IP в исходящих (src → VPN server IP)
 * 2. Перезаписи dst IP в входящих (server IP → device IP)
 * 3. Пересчёта IP/TCP контрольных сумм
 * <p>
 * В этом учебном проекте сервер сам делает форвардинг,
 * поэтому клиент передаёт пакеты «как есть», и сервер
 * заменяет src-адрес на свой собственный перед отправкой.
 * ────────────────────────────────────────────────────────────────
 */
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
     * <p>
     * В учебном проекте — просто возвращает пакет как есть,
     * т.к. сервер уже правильно адресует ответы.
     * В реальном: нужно переписать dst IP + пересчитать чексуммы.
     *
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
