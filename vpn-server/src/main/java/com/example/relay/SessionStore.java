package com.example.relay;

import com.example.shared.IpHeader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

/**
 * Хранилище активных TcpRelay для одного VPN-клиента.
 * <p>
 * Ключ: srcIP:srcPort-dstIP:dstPort
 */
public class SessionStore {

    private static final Logger log = LoggerFactory.getLogger(SessionStore.class);
    private static final long SESSION_TTL = 10 * 60 * 1000L;  // 10 минут

    private final Map<String, TcpRelay> relays = new ConcurrentHashMap<>();

    /**
     * Возвращает существующий relay или создаёт новый через factory.
     */
    public TcpRelay getOrCreate(IpHeader ipHeader, Supplier<TcpRelay> factory) {
        String key = ipHeader.getNatKey();
        TcpRelay relay = relays.get(key);

        if (relay == null || !relay.isActive()) {
            relay = factory.get();
            relays.put(key, relay);
            log.debug("New relay: {}", key);
        }
        return relay;
    }

    public TcpRelay get(String key) {
        return relays.get(key);
    }

    public void remove(String key) {
        relays.remove(key);
    }

    /**
     * Закрывает все активные relay (вызывается при отключении клиента).
     */
    public void closeAll() {
        log.info("Closing {} relays", relays.size());
        relays.values().forEach(TcpRelay::close);
        relays.clear();
    }

    /**
     * Удаляет устаревшие relay.
     */
    public void evictExpired() {
        long now = System.currentTimeMillis();
        relays.entrySet().removeIf(e -> {
            TcpRelay relay = e.getValue();
            boolean expired = !relay.isActive()
                    || (now - relay.getSession().getLastActivity()) > SESSION_TTL;
            if (expired) {
                relay.close();
                log.debug("Evicted relay: {}", e.getKey());
            }
            return expired;
        });
    }

    public int size() {
        return relays.size();
    }
}

