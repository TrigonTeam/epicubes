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
    public void processIncoming(byte[] bytes) {
        this.x = (short) ((bytes[0] << 8) | (bytes[1] & 0xFF));
        this.y = (short) ((bytes[2] << 8) | (bytes[3] & 0xFF));
    }

    @Override
    public byte[] processOutgoing() {
        return new byte[] { (byte) ((x >> 8) & 0xFF), (byte) (x & 0xFF), (byte) ((y >> 8) & 0xFF), (byte) (y & 0xFF) };
    }
}
