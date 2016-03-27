package cz.trigon.ecubes.server;

import cz.trigon.ecubes.net.ServerNetcode;
import cz.trigon.ecubes.net.packet.Packet;

import java.io.IOException;

public class EpiServer implements Runnable {

    protected int tps = 20;
    protected long time, lastTime, magicConstant = 1000000000;
    protected double tickTime = 1d / tps;
    protected double tickTimeSec = this.tickTime * this.magicConstant;

    private ServerNetcode server;

    public EpiServer() throws IOException {
        this.server = new ServerNetcode(6927, 6928);
    }

    private void tick() {
        Packet p;
        while((p = this.server.getProcessedPackets().poll()) != null) {
            this.server.sendPacketToAll(p, true);
        }
    }

    @Override
    public void run() {
        this.time = System.nanoTime();
        this.lastTime = time;

        while(true) {
            this.time = System.nanoTime();
            while (time - lastTime >= this.tickTimeSec) {
                this.tick();
                lastTime += this.tickTimeSec;
            }
        }
    }
}
