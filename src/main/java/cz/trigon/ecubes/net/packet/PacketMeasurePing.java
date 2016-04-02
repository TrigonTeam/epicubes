package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;
import cz.trigon.ecubes.util.NumberPacker;

public class PacketMeasurePing extends Packet {
    private int timeMs = 0;
    private int pingMs;

    public PacketMeasurePing(Connection connection, int id) {
        super(connection, id);
    }

    public PacketMeasurePing() {
        super();
    }

    public int getPing() {
        return this.pingMs;
    }

    @Override
    public void processIncoming(byte[] bytes, boolean onServer) {
        if(onServer) {
            this.timeMs = NumberPacker.unpackInt(bytes);
        } else {
            this.pingMs = (int) (System.nanoTime() / 1000000f) - NumberPacker.unpackInt(bytes);
        }
    }

    @Override
    public byte[] processOutgoing(boolean onServer) {
        return NumberPacker.pack(onServer ? this.timeMs : (int) (System.nanoTime() / 1000000f));
    }
}
