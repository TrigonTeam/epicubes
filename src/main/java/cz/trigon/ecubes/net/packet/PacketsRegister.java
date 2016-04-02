package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;

import java.util.HashMap;
import java.util.Map;

public class PacketsRegister {
    public PacketsRegister() {
        this.packets = new HashMap<>();
        this.pids = new HashMap<>();

        this.registerPacket(PacketMeasurePing.class, (short) 1);
        this.registerPacket(PacketControl.class, (short) 2);
    }

    private Map<Short, Class<? extends Packet>> packets;
    private Map<Class<? extends Packet>, Short> pids;

    public void registerPacket(Class<? extends Packet> packet, short id) {
        if(!this.packets.containsKey(id)) {
            this.packets.put(id, packet);
            this.pids.put(packet, id);
        }
    }

    public Class<? extends Packet> getPacket(short id) {
        return this.packets.get(id);
    }

    public short getPacketId(Class<? extends Packet> packet) {
        return this.pids.get(packet);
    }

    public Packet createPacket(Connection connection, short id) {
        Class<? extends Packet> c = this.getPacket(id);
        if(c != null) {
            try {
                return c.getConstructor(Connection.class, int.class).newInstance(connection, id);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
