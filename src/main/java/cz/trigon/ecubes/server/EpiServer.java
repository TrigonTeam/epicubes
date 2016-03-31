package cz.trigon.ecubes.server;

import cz.trigon.ecubes.net.ServerNetcode;
import cz.trigon.ecubes.net.packet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class EpiServer implements Runnable {

    protected int tps = 20;
    protected long time, lastTime, magicConstant = 1000000000;
    protected double tickTime = 1d / tps;
    protected double tickTimeSec = this.tickTime * this.magicConstant;

    private ServerNetcode server;

    private List<short[]> points = new ArrayList<>();
    private final short maxDim = 100;

    public EpiServer() throws IOException {
        this.server = new ServerNetcode(6927, 6928);
    }

    private void tick() {
        Packet p;
        while ((p = this.server.getProcessedPackets().poll()) != null) {
            if (p.getId() == 1) {
                this.server.sendPacket(p.getConnection().getID(), p, true);
            } else if (p.getId() == 2) {
                PacketCommand cmd = (PacketCommand) p;
                if(cmd.getCommand() == 0xAA) {
                    this.points.clear();

                    for(short x = 0; x < 800; x++) {
                        for(short y = 0; y < 600; y++) {
                            this.points.add(new short[] { x, y, 255, 255, 255 });
                        }
                    }
                }
            } else if (p.getId() == 100) {
                PacketDrawTest d = (PacketDrawTest) p;
                this.points.add(new short[]{d.x, d.y, d.r, d.g, d.b});
                this.server.sendPacketToAll(p, false);
            } else if (p.getId() == 101) {
                PacketHistory req = (PacketHistory) p;
                this.sendHistory(req);
            }
        }
    }

    private void sendHistory(PacketHistory req) {
        PacketHistory h = new PacketHistory(this.points.stream().filter(sh -> (sh[0] >= req.getX()
                && sh[0] < req.getX() + this.maxDim
                && sh[1] >= req.getY()
                && sh[1] < req.getY() + this.maxDim)).collect(Collectors.toList()),
                (short) (req.getX() + this.maxDim),
                (short) (req.getY() + this.maxDim));

        this.server.sendPacket(req.getConnection().getID(), h, true);
    }

    @Override
    public void run() {
        this.time = System.nanoTime();
        this.lastTime = time;

        while (true) {
            this.time = System.nanoTime();
            while (time - lastTime >= this.tickTimeSec) {
                this.tick();
                lastTime += this.tickTimeSec;
            }
        }
    }
}
