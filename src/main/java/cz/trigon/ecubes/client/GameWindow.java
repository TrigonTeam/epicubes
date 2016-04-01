package cz.trigon.ecubes.client;

import cz.trigon.ecubes.net.ClientNetcode;
import cz.trigon.ecubes.net.packet.*;
import org.lwjgl.BufferUtils;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.nio.DoubleBuffer;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;


public class GameWindow implements Runnable {

    private long windowHandle;
    private GLFWErrorCallback errorCallback;
    private int width, height;
    private int tps = 20, magicConstant = 1000000000;
    private double tickTime = 1d / tps;
    private double tickTimeSec = this.tickTime * this.magicConstant;
    private long time, lastTime, lastInfo;
    private int fps, ticks, lastTicks;
    private int shutdownCode;
    private Random rnd = new Random();

    private List<short[]> points = new ArrayList<>();
    private List<int[]> localPoints = new ArrayList<>();

    private ClientNetcode client;
    private byte r, g, b;

    private void renderTick(float ptt) {
        int mouse = GLFW.glfwGetMouseButton(this.windowHandle, GLFW.GLFW_MOUSE_BUTTON_1);
        if (mouse == 1) {
            DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
            GLFW.glfwGetCursorPos(this.windowHandle, xpos, ypos);

            int[] i = this.localPoints.size() > 0 ? this.localPoints.get(this.localPoints.size() - 1) : null;
            short x = (short) xpos.get(0);
            short y = (short) ypos.get(0);

            if (i == null || !(i[0] == x && i[1] == y)) {
                this.localPoints.add(new int[]{x, y});
                PacketDrawTest d = new PacketDrawTest(x, y, this.r, this.g, this.b);

                this.client.sendPacket(d, false);
            }
        }

        GL11.glPointSize(5f);

        GL11.glBegin(GL11.GL_POINTS);
        GL11.glColor3f(0, 1f, 0);
        for (int[] pn : this.localPoints) {
            GL11.glVertex2f(pn[0], pn[1]);
        }

        for (short[] pn : this.points) {
            GL11.glColor3f(pn[2] / 255f, pn[3] / 255f, pn[4] / 255f);
            GL11.glVertex2f(pn[0], pn[1]);
        }

        GL11.glEnd();
    }

    private void tick() {
        this.handleControls();

        if (this.ticks % 100 == 0) {
            this.client.sendPacket(new PacketMeasurePing(), true);
        }

        Packet p;
        while ((p = this.client.getProcessedPackets().poll()) != null) {
            if (p.getId() == 100) {
                PacketDrawTest d = ((PacketDrawTest) p);
                this.points.add(new short[]{d.x, d.y, (short) (d.r + 127), (short) (d.g + 127), (short) (d.b + 127)});
            } else if (p.getId() == 101) {
                PacketHistory h = (PacketHistory) p;
                this.points.addAll(h.getHistory());
            } else if (p.getId() == 1) {
                System.out.println("Ping: " + ((PacketMeasurePing) p).getPing() + " ms");
            } else if (p.getId() == 2) {
                PacketCommand cmd = (PacketCommand) p;

                if(cmd.getCommand() == 0xBA) {
                    // 0xBA - reload history
                    this.loadHistory();
                }
            }
        }
    }

    private void handleControls() {
        if (GLFW.glfwGetKey(this.windowHandle, GLFW.GLFW_KEY_H) == GLFW.GLFW_PRESS) {
            this.loadHistory();
        }

        if (GLFW.glfwGetKey(this.windowHandle, GLFW.GLFW_KEY_C) == GLFW.GLFW_PRESS) {
            // 0xAB - clear canvas
            this.client.sendPacket(new PacketCommand(0xAB), true);
            this.loadHistory();
        }

        if (GLFW.glfwGetKey(this.windowHandle, GLFW.GLFW_KEY_V) == GLFW.GLFW_PRESS) {
            this.localPoints.clear();
        }

        if (GLFW.glfwGetKey(this.windowHandle, GLFW.GLFW_KEY_F) == GLFW.GLFW_PRESS) {
            // 0xAA - fill canvas
            this.client.sendPacket(new PacketCommand(0xAA), true);
        }

        int mouse = GLFW.glfwGetMouseButton(this.windowHandle, GLFW.GLFW_MOUSE_BUTTON_2);
        if (mouse == 1) {
            this.r = (byte) (127 - rnd.nextInt(256));
            this.g = (byte) (127 - rnd.nextInt(256));
            this.b = (byte) (127 - rnd.nextInt(256));
        }
    }

    private void loadHistory() {
        this.points.clear();

        for (int x = 0; x < this.width; x += 100) {
            for (int y = 0; y < this.height; y += 100) {
                this.client.sendPacket(new PacketHistory((short) x, (short) y), true);
            }
        }
    }

    private void loopTick() {
        float ptt = (this.time - this.lastTime) / ((float) this.tickTimeSec);
        this.renderTick(ptt);

        this.fps++;

        this.time = System.nanoTime();
        while (time - lastTime >= this.tickTimeSec) {
            this.tick();

            this.ticks++;
            lastTime += this.tickTimeSec;
        }

        if (this.time - this.lastInfo >= this.magicConstant) {
            this.lastInfo += this.magicConstant;
            this.fpsCount();
            this.fps = 0;
        }
    }

    public void loop() {
        GL.createCapabilities();
        this.testInit();

        this.time = System.nanoTime();
        this.lastTime = time;
        this.lastInfo = time;

        while (GLFW.glfwWindowShouldClose(this.windowHandle) == GL11.GL_FALSE) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            this.loopTick();
            GLFW.glfwSwapBuffers(this.windowHandle);
            GLFW.glfwPollEvents();
        }

        this.cleanup();
        System.exit(this.shutdownCode);
    }

    private void init() {
        GLFW.glfwSetErrorCallback(this.errorCallback = GLFWErrorCallback.createPrint(System.err));

        if (GLFW.glfwInit() != GL11.GL_TRUE) {
            throw new RuntimeException("Couldn't init GLFW.");
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);

        this.windowHandle = GLFW.glfwCreateWindow(800, 600, "EpiCubes", 0, 0);

        if (this.windowHandle == 0) {
            throw new RuntimeException("Couldn't create window.");
        }


        GLFW.glfwHideWindow(this.windowHandle);
        this.resize(800, 600);
        GLFW.glfwShowWindow(this.windowHandle);
    }

    private void testInit() {
        try {
            this.client = new ClientNetcode("localhost", 6927, 6928);
            this.client.connect(3);
        } catch (IOException e) {
            e.printStackTrace();
        }

        this.r = (byte) (127 - rnd.nextInt(256));
        this.g = (byte) (127 - rnd.nextInt(256));
        this.b = (byte) (127 - rnd.nextInt(256));

        GL11.glViewport(0, 0, width, height);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 0, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void fpsCount() {
        GLFW.glfwSetWindowTitle(this.windowHandle, "EpiCubes - " + this.fps + " FPS");
    }

    private void cleanup() {
        this.client.stop();
    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        GLFWVidMode vidmode = GLFW.glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        GLFW.glfwSetWindowPos(this.windowHandle,
                (vidmode.width() - width) / 2,
                (vidmode.height() - height) / 2);

        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GLFW.glfwSwapInterval(0);
    }

    public void shutdown(int code) {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, GL11.GL_TRUE);
        this.shutdownCode = code;
    }

    @Override
    public void run() {
        try {
            this.init();
            this.loop();
        } catch (Exception e) {
            e.printStackTrace();
        }

        this.cleanup();
        System.exit(this.shutdownCode);
    }
}
