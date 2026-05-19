package com.example.vpn.core;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * NAT-таблица (Network Address Translation).
 * <p>
 * Хранит маппинг:
 * srcIP:srcPort → dstIP:dstPort  (исходящие)
 * dstIP:dstPort → srcIP:srcPort  (обратный lookup)
 * <p>
 * В реальном VPN NAT значительно сложнее (port mapping, timeouts, ICMP...).
 * Здесь — учебная упрощённая версия.
 */
public class NatTable {

    private static final String TAG = "NatTable";
    private static final long SESSION_TTL = 5 * 60 * 1000L;  // 5 минут

    // key → Session
    private final Map<String, Session> table = new ConcurrentHashMap<>();

    public void put(Session session) {
        table.put(session.key(), session);
        Log.d(TAG, "NAT entry added: " + session);
    }

    public Session get(String key) {
        return table.get(key);
    }

    public void remove(String key) {
        table.remove(key);
    }

    /**
     * Удаляет устаревшие сессии (вызывать периодически).
     */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        table.entrySet().removeIf(e -> {
            boolean expired = (now - e.getValue().getLastActivity()) > SESSION_TTL
                    || e.getValue().getState() == Session.State.CLOSED;
            if (expired) Log.d(TAG, "NAT evicted: " + e.getKey());
            return expired;
        });
    }

    public int size() {
        return table.size();
    }
}