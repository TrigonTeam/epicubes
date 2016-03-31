package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;

public class PacketDrawTest extends Packet {

    public short x, y;

    public PacketDrawTest(Connection connection) {
        super(connection);
    }

    public PacketDrawTest(short x, short y) {
        super();
        this.x = x;
        this.y = y;
    }

    @Override
    public void processIncoming(byte[] bytes, boolean onServer) {
        this.x = (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
        this.y = (short) ((bytes[2] << 8) | (bytes[3] & 0xFF));
    }

    @Override
    public byte[] processOutgoing(boolean onServer) {
        byte[] b = new byte[4];

        b[0] = (byte) ((x >> 8) & 0xFF);
        b[1] = (byte) (x & 0xFF);
        b[2] = (byte) ((y >> 8) & 0xFF);
        b[3] = (byte) (y & 0xFF);

        return b;
    }
}
