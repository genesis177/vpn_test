package com.example.vpn.core;

/**
 * Одна NAT-сессия: отслеживает соответствие между
 * локальным адресом клиента и адресом назначения.
 */
public class Session {

    public enum State {SYN_SENT, ESTABLISHED, FIN_WAIT, CLOSED}

    public final String srcIp;
    public final int srcPort;
    public final String dstIp;
    public final int dstPort;
    public final int protocol;   // TCP=6, UDP=17

    private volatile State state = State.SYN_SENT;
    private long lastActivity = System.currentTimeMillis();

    public Session(String srcIp, int srcPort, String dstIp, int dstPort, int protocol) {
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
        this.protocol = protocol;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public void touch() {
        lastActivity = System.currentTimeMillis();
    }

    /**
     * Ключ для NAT-таблицы
     */
    public String key() {
        return srcIp + ":" + srcPort + "-" + dstIp + ":" + dstPort + "/" + protocol;
    }

    @Override
    public String toString() {
        return String.format("Session[%s:%d→%s:%d proto=%d state=%s]",
                srcIp, srcPort, dstIp, dstPort, protocol, state);
    }
}