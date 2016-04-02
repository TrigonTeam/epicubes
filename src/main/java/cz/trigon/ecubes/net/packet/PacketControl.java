package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;
import cz.trigon.ecubes.util.NumberPacker;

public class PacketControl extends Packet {
    protected int command;

    public PacketControl(Connection connection, int id) {
        super(connection, id);
    }

    public PacketControl(int command) {
        super();
        this.command = command;
    }

    public int getCommand() {
        return this.command;
    }

    @Override
    public void processIncoming(byte[] bytes, boolean onServer) {
        this.command = NumberPacker.unpackInt(bytes);
    }

    @Override
    public byte[] processOutgoing(boolean onServer) {
        return NumberPacker.pack(this.command);
    }
}
