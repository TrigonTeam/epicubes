package cz.trigon.ecubes.net;

import com.esotericsoftware.kryonet.Client;
import com.esotericsoftware.kryonet.Connection;
import com.esotericsoftware.kryonet.Listener;
import cz.trigon.ecubes.client.GameWindow;
import cz.trigon.ecubes.net.packet.Packet;
import cz.trigon.ecubes.net.packet.PacketDrawTest;
import cz.trigon.ecubes.net.packet.PacketRegister;
import org.xerial.snappy.Snappy;

import java.io.IOException;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class ClientNetcode extends Listener {
    private Queue<IncomingPacketBlob> toProcessIncoming = new ConcurrentLinkedQueue<>();
    private Queue<Packet> processedIncoming = new ConcurrentLinkedQueue<>();
    private Queue<OutgoingPacketBlob> toProcessOutgoing = new ConcurrentLinkedQueue<>();

    private Client client;
    private Thread processing;

    public ClientNetcode(String address, int tcp, int udp) throws IOException {
        this.processing = new Thread(this::process);

        this.client = new Client(64000, 64000);
        this.client.start();
        this.client.getKryo().register(byte[].class);
        this.client.connect(5000, address, tcp, udp);
        this.client.addListener(this);

        this.processing.start();
    }

    public Queue<Packet> getProcessedPackets() {
        return this.processedIncoming;
    }

    public void stop() {
        this.processing.interrupt();
        this.client.stop();
    }

    public void sendPacket(Packet packet, boolean tcp) {
        this.toProcessOutgoing.add(new OutgoingPacketBlob(packet, tcp));
        try {
            Thread.sleep(1);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
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

                byte[] bytes = b.packet.processOutgoing(false);
                if (bytes.length >= 128) {
                    bytes = Snappy.compress(bytes);
                    ids |= 1;
                }

                byte[] withMeta = new byte[bytes.length + 2];
                System.arraycopy(bytes, 0, withMeta, 2, bytes.length);

                withMeta[0] = (byte) (ids & 0xFF);
                withMeta[1] = (byte) ((ids >> 8) & 0xFF);

                if (b.tcp) {
                    this.client.sendTCP(withMeta);
                } else {
                    this.client.sendUDP(withMeta);
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

                    p.processIncoming(packetData, false);
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
        public Packet packet;
        public boolean tcp;

        public OutgoingPacketBlob(Packet packet, boolean useTcp) {
            this.packet = packet;
            this.tcp = useTcp;
        }
    }
}
