package cz.trigon.ecubes.net.packet;


import com.esotericsoftware.kryonet.Connection;

public abstract class Packet {
    protected boolean hasFinished = false;
    protected boolean enabledTcp = true;
    protected boolean enabledUdp = true;
    protected Connection connection;

    public Packet() { };

    public Packet(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean hasFinishedProcessing() {
        return this.hasFinished;
    }

    public boolean canTcp() {
        return this.enabledTcp;
    }

    public boolean canUdp() {
        return this.enabledUdp;
    }

    public abstract void processIncoming(byte[] bytes);

    public abstract byte[] processOutgoing();
}
