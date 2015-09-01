package cz.trigon.ecubes.net.packet;


import com.esotericsoftware.kryonet.Connection;

public abstract class Packet {
    protected boolean hasFinished = false;
    protected Connection connection;

    public Packet(Connection connection) {
        this.connection = connection;
    }

    public Connection getConnection() {
        return this.connection;
    }

    public boolean hasFinishedProcessing() {
        return this.hasFinished;
    }

    public abstract void useIncoming();
    public abstract void processIncoming(byte[] bytes);
    public abstract byte[] processOutgoing();
}
