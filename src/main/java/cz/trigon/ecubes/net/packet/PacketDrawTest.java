package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;

public class PacketDrawTest extends Packet {

    public short x, y;
    public long timeSent;

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

        for (int i = 0; i < 8; i++) {
            this.timeSent <<= 8;
            this.timeSent |= (bytes[i + 4] & 0xFF);
        }
    }

    @Override
    public byte[] processOutgoing() {
        byte[] b = new byte[12];

        b[0] = (byte) ((x >> 8) & 0xFF);
        b[1] = (byte) (x & 0xFF);
        b[2] = (byte) ((y >> 8) & 0xFF);
        b[3] = (byte) (y & 0xFF);

        long l = this.timeSent;
        for (int i = 7; i >= 0; i--) {
            b[i + 4] = (byte)(l & 0xFF);
            l >>= 8;
        }

        return b;
    }
}
