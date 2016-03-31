package cz.trigon.ecubes.net;

import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import com.esotericsoftware.kryonet.Server;
import cz.trigon.ecubes.net.packet.Packet;
import cz.trigon.ecubes.net.packet.PacketRegister;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class ServerNetcode extends Listener {

    private Queue<IncomingPacketBlob> toProcessIncoming = new ConcurrentLinkedQueue<>();
    private Queue<Packet> processedIncoming = new ConcurrentLinkedQueue<>();
    private Queue<OutgoingPacketBlob> toProcessOutgoing = new ConcurrentLinkedQueue<>();

    private Server server;
    private Thread processing;

    public ServerNetcode(int tcp, int udp) throws IOException {
        this.processing = new Thread(this::process);

        this.server = new Server(64000, 64000);
        this.server.start();
        this.server.getKryo().register(byte[].class);
        this.server.bind(tcp, udp);
        this.server.addListener(this);

        this.processing.start();
    }

    public void stop() {
        this.processing.interrupt();
        this.server.stop();
    }

    @Override
    public void connected(Connection connection) {
        System.out.println("Connected #" + connection.getID() + ":" +
                connection.getRemoteAddressTCP().getHostName() + "/" +
                connection.getRemoteAddressTCP().getPort());
    }

    @Override
    public void disconnected(Connection connection) {
        System.out.println("Disconnected #" + connection.getID());
    }

    public Queue<Packet> getProcessedPackets() {
        return this.processedIncoming;
    }

    public void sendPacket(int clientId, Packet packet, boolean tcp) {
        this.toProcessOutgoing.add(new OutgoingPacketBlob(clientId, false, false, packet, tcp));
    }

    public void sendPacketToAll(Packet packet, boolean tcp) {
        this.toProcessOutgoing.add(new OutgoingPacketBlob(0, false, true, packet, tcp));

    }

    public void sendPacketToAllExcept(int exceptClientId, Packet packet, boolean tcp) {
        this.toProcessOutgoing.add(new OutgoingPacketBlob(exceptClientId, true, true, packet, tcp));
    }

    private void process() {
        while (true) {
            this.processOutgoing();
            this.processIncoming();

            try {
                Thread.sleep(1);
            } catch (InterruptedException ignored) {}
        }
    }

    private void processOutgoing() {
        OutgoingPacketBlob b;

        while ((b = this.toProcessOutgoing.poll()) != null) {
            try {
                short id = PacketRegister.getPacketId(b.packet.getClass());
                int ids = id << 1;

                byte[] bytes = b.packet.processOutgoing(true);
                if (bytes.length >= 128) {
                    System.out.println("Previous size: " + bytes.length);
                    bytes = Snappy.compress(bytes);
                    System.out.println("Compressed size: " + bytes.length);
                    ids |= 1;
                }

                byte[] withMeta = new byte[bytes.length + 2];
                System.arraycopy(bytes, 0, withMeta, 2, bytes.length);

                withMeta[0] = (byte) (ids & 0xFF);
                withMeta[1] = (byte) ((ids >> 8) & 0xFF);

                if (b.toAll) {
                    if (b.except) {
                        if (b.tcp) {
                            this.server.sendToAllExceptTCP(b.clientId, withMeta);
                        } else {
                            this.server.sendToAllExceptTCP(b.clientId, withMeta);
                        }
                    } else {
                        if (b.tcp) {
                            this.server.sendToAllTCP(withMeta);
                        } else {
                            this.server.sendToAllUDP(withMeta);
                        }
                    }
                } else {
                    if (b.tcp) {
                        this.server.sendToTCP(b.clientId, withMeta);
                    } else {
                        this.server.sendToUDP(b.clientId, withMeta);
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private void processIncoming() {
        IncomingPacketBlob b;

        while ((b = this.toProcessIncoming.poll()) != null) {
            try {
                byte[] data = b.rawData;
                int ids = ((data[0] & 0xFF) | (data[1] << 8) & 0xFFFE);
                short id = (short) (ids >> 1);

                byte[] packetData = new byte[data.length - 2];
                Packet p = PacketRegister.createPacket(b.connection, id);

                if (p != null) {
                    System.arraycopy(data, 2, packetData, 0, packetData.length);

                    if ((data[0] & 1) == 1) {
                        packetData = Snappy.uncompress(packetData);
                    }

                    p.processIncoming(packetData, true);
                    this.processedIncoming.add(p);
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    @Override
    public void received(Connection connection, Object o) {
        if (o instanceof byte[]) {
            this.toProcessIncoming.add(new IncomingPacketBlob(connection, (byte[]) o));
        }
    }

    private class IncomingPacketBlob {
        public Connection connection;
        public byte[] rawData;

        public IncomingPacketBlob(Connection connection, byte[] data) {
            this.connection = connection;
            this.rawData = data;
        }
    }

    private class OutgoingPacketBlob {
        public int clientId;
        public Packet packet;
        public boolean tcp;
        public boolean toAll;
        public boolean except;

        public OutgoingPacketBlob(int clientId, boolean ignore, boolean all, Packet packet, boolean useTcp) {
            this.clientId = clientId;
            this.packet = packet;
            this.tcp = useTcp;
            this.except = ignore;
            this.toAll = all;
        }
    }


}
