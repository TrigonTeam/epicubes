package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;

public class PacketTest extends Packet {

    private static short NUM = 0;

    private long time = 0;
    private short num;

    public PacketTest(Connection connection) {
        super(connection);
    }

    public PacketTest() {
        super();
    }

    @Override
    public void processIncoming(byte[] bytes, boolean onServer) {
        for (int i = 0; i < 8; i++) {
            this.time <<= 8;
            this.time |= (bytes[i] & 0xFF);
        }

        this.num = (short)((bytes[8] << 8) | (bytes[9] & 0xFF));

        if(onServer) {
            System.out.println("Received packet #" + this.num);
        } else {
            System.out.println("RECV #" + this.num + " in " + (System.nanoTime() - this.time) / 1000000f + " ms");
        }
    }

    @Override
    public byte[] processOutgoing(boolean onServer) {
        System.out.println(onServer ? "Sent packet back from the server" : "SEND #" + ++PacketTest.NUM);

        byte[] b = new byte[10];
        long l = onServer ? this.time : System.nanoTime();
        for (int i = 7; i >= 0; i--) {
            b[i] = (byte) (l & 0xFF);
            l >>= 8;
        }
        b[8] = (byte)(((onServer ? this.num : PacketTest.NUM) >> 8) & 0xFF);
        b[9] = (byte)((onServer ? this.num : PacketTest.NUM) & 0xFF);
        return b;
    }
}
