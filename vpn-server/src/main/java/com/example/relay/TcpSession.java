package com.example.relay;

/**
 * TCP-сессия на стороне сервера.
 * Отслеживает состояние одного TCP-соединения.
 */
public class TcpSession {

    public enum State {SYN_RECEIVED, ESTABLISHED, FIN_WAIT, CLOSED}

    public final String srcIp;
    public final int srcPort;
    public final String dstIp;
    public final int dstPort;

    private volatile State state = State.SYN_RECEIVED;
    private long lastActivity = System.currentTimeMillis();

    private long seqNum = 1000L;
    private long ackNum = 0L;

    public TcpSession(String srcIp, int srcPort, String dstIp, int dstPort) {
        this.srcIp = srcIp;
        this.srcPort = srcPort;
        this.dstIp = dstIp;
        this.dstPort = dstPort;
    }

    public State getState() {
        return state;
    }

    public void setState(State s) {
        this.state = s;
    }

    public void touch() {
        lastActivity = System.currentTimeMillis();
    }

    public long getLastActivity() {
        return lastActivity;
    }

    public long getSeqNum() {
        return seqNum;
    }

    public long getAckNum() {
        return ackNum;
    }

    public void advanceSeq(int bytes) {
        seqNum += bytes;
    }

    public void setAckNum(long ack) {
        ackNum = ack;
    }

    public String key() {
        return srcIp + ":" + srcPort + "-" + dstIp + ":" + dstPort;
    }

    @Override
    public String toString() {
        return String.format("TcpSession[%s:%d→%s:%d %s]",
                srcIp, srcPort, dstIp, dstPort, state);
    }
}
