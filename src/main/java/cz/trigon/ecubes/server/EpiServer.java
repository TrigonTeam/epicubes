package cz.trigon.ecubes.server;

import cz.trigon.ecubes.exception.EpicubesException;
import cz.trigon.ecubes.exception.ExceptionUtil;
import cz.trigon.ecubes.log.EpiLogger;
import cz.trigon.ecubes.net.ServerNetcode;
import cz.trigon.ecubes.net.packet.*;

import java.io.IOException;

public class EpiServer implements Runnable {

    private int tps = 20;
    private long time, lastTime, magicConstant = 1000000000;
    private double tickTime = 1d / tps;
    private double tickTimeSec = this.tickTime * this.magicConstant;
    private int ticks;
    private int shutdownCode = 0;
    private boolean run = true;

    private ServerNetcode server;

    public EpiServer() throws IOException {
        this.server = new ServerNetcode(6927, 6928);
    }

    private void init() {
        EpiLogger.init(this.getClass());
    }

    private void tick() {
        Packet p;
        while ((p = this.server.getProcessedPackets().poll()) != null) {
            if (p.getId() == 1) {
                this.server.sendPacket(p.getConnection().getID(), p, true);
            } else if (p.getId() == 2) {
                PacketControl cmd = (PacketControl) p;
                // command logic
            }
        }
    }

    private void handleFeatureException(EpicubesException e) {

    }

    private void cleanup() {

    }

    private void loop() {
        this.time = System.nanoTime();
        this.lastTime = time;

        while (this.run) {
            try {
                this.time = System.nanoTime();
                while (time - lastTime >= this.tickTimeSec) {
                    this.tick();
                    this.ticks++;
                    lastTime += this.tickTimeSec;
                }
            } catch (EpicubesException e) {
                if(e.getUrgency() == EpicubesException.Urgency.BREAKING) {
                    throw e;
                } else if(e.getUrgency() == EpicubesException.Urgency.FEATURE_BREAKING) {
                    this.handleFeatureException(e);
                }
            } catch (Exception e) {
                throw ExceptionUtil.gameBreakingException(e, this, true);
            }
        }

        this.endMyLife();
    }

    public void shutdown(int code) {
        this.shutdownCode = code;
        this.run = false;
    }

    public ServerNetcode getNet() {
        return this.server;
    }

    @Override
    public void run() {
        try {
            this.init();
            EpiLogger.info("Starting up");
            this.loop();
        } catch (EpicubesException e) {
            this.shutdown(e.getErrorCode());
        }

        this.endMyLife();
    }

    private void endMyLife() {
        this.cleanup();
        if(this.shutdownCode == 0)
            EpiLogger.info("Shutting down nicely");
        else
            EpiLogger.warning("Shutting down with error code " + this.shutdownCode);

        System.exit(this.shutdownCode);
    }
}
