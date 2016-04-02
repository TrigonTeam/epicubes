package cz.trigon.ecubes.net.packet;


import com.esotericsoftware.kryonet.Connection;

public abstract class Packet {
    protected boolean hasFinished = false;
    protected boolean enabledTcp = true;
    protected boolean enabledUdp = true;
    protected final boolean received;
    private int id;

    protected Connection connection;

    public Packet() {
        this.received = false;
    }

    public Packet(Connection connection, int id) {
        this.connection = connection;
        this.received = true;
        this.id = id;
    }

    public int getId() {
        return this.id;
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

    public abstract void processIncoming(byte[] bytes, boolean onServer);

    public abstract byte[] processOutgoing(boolean onServer);
}
