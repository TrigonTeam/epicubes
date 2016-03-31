package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;
import cz.trigon.ecubes.util.NumberPacker;

import java.util.ArrayList;
import java.util.List;

public class PacketHistory extends Packet {

    private List<short[]> history;
    private short x, y;

    public PacketHistory(Connection connection) {
        super(connection);
        this.history = new ArrayList<>();
    }

    public PacketHistory(List<short[]> history, short xOffset, short yOffset) {
        this(xOffset, yOffset);
        this.history = history;
    }

    public PacketHistory(short xStart, short yStart) {
        super();
        this.x = xStart;
        this.y = yStart;
    }

    public List<short[]> getHistory() {
        return this.history;
    }

    public short getX() { return this.x; }

    public short getY() { return this.y; }

    @Override
    public void processIncoming(byte[] bytes, boolean onServer) {
        if (onServer) {
            this.x = NumberPacker.unpackShort(bytes);
            this.y = NumberPacker.unpackShort(bytes, 2);
        } else {
            int offset = bytes.length - (bytes.length % 7);

            for (int i = 0; i < offset; i += 7) {
                short[] point = new short[5];
                point[0] = NumberPacker.unpackShort(bytes, i);
                point[1] = NumberPacker.unpackShort(bytes, i + 2);
                point[2] = (short) (bytes[i + 4] + 127);
                point[3] = (short) (bytes[i + 5] + 127);
                point[4] = (short) (bytes[i + 6] + 127);
                this.history.add(point);
            }

            this.x = NumberPacker.unpackShort(bytes, offset);
            this.y = NumberPacker.unpackShort(bytes, offset + 2);
        }
    }

    @Override
    public byte[] processOutgoing(boolean onServer) {
        byte[] x = NumberPacker.pack(this.x);
        byte[] y = NumberPacker.pack(this.y);

        if (onServer) {
            byte[] bytes = new byte[(this.history.size() * 7) + 4];
            int pt = 0;

            for (short[] sh : this.history) {
                for (int i = 0; i < 2; i++) {
                    byte[] shBytes = NumberPacker.pack(sh[i]);
                    bytes[pt++] = shBytes[0];
                    bytes[pt++] = shBytes[1];
                }

                bytes[pt++] = (byte) sh[2];
                bytes[pt++] = (byte) sh[3];
                bytes[pt++] = (byte) sh[4];
            }

            bytes[pt++] = x[0];
            bytes[pt++] = x[1];
            bytes[pt++] = y[0];
            bytes[pt] = y[1];

            return bytes;
        } else {
            return new byte[] { x[0], x[1], y[0], y[1] };
        }
    }
}
