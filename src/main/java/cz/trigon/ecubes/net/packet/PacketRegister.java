package cz.trigon.ecubes.net.packet;

import com.esotericsoftware.kryonet.Connection;

import java.util.HashMap;
import java.util.Map;

public class PacketRegister {
    private static Map<Short, Class<? extends Packet>> packets = new HashMap<>();
    private static Map<Class<? extends Packet>, Short> pids = new HashMap<>();

    public static void registerPacket(Class<? extends Packet> packet, short id) {
        if(!PacketRegister.packets.containsKey(id)) {
            PacketRegister.packets.put(id, packet);
            PacketRegister.pids.put(packet, id);
        }
    }

    public static Class<? extends Packet> getPacket(short id) {
        return PacketRegister.packets.get(id);
    }

    public static short getPacketId(Class<? extends Packet> packet) {
        return PacketRegister.pids.get(packet);
    }

    public static Packet createPacket(Connection connection, short id) {
        Class<? extends Packet> c = PacketRegister.getPacket(id);
        if(c != null) {
            try {
                return c.getConstructor(Connection.class).newInstance(connection);
            } catch (Exception e) {
                e.printStackTrace();
                return null;
            }
        } else {
            return null;
        }
    }
}
