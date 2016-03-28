package cz.trigon.ecubes.client;

import cz.trigon.ecubes.net.ClientNetcode;
import cz.trigon.ecubes.net.packet.Packet;
import cz.trigon.ecubes.net.packet.PacketDrawTest;
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


public class GameWindow implements Runnable {

    private long windowHandle;
    private GLFWErrorCallback errorCallback;
    private int width, height;
    protected int tps = 20, magicConstant = 1000000000;
    protected double tickTime = 1d / tps;
    protected double tickTimeSec = this.tickTime * this.magicConstant;
    protected long time, lastTime, lastInfo;
    protected int fps, ticks, lastTicks;
    private int shutdownCode;

    public void shutdown(int code) {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, GL11.GL_TRUE);
        this.shutdownCode = code;
    }

    private List<short[]> points = new ArrayList<>();
    private List<int[]> localPoints = new ArrayList<>();

    private ClientNetcode client;

    int num = 1;
    float timePacket;

    private void renderTick(float ptt) {
        int mouse = GLFW.glfwGetMouseButton(this.windowHandle, GLFW.GLFW_MOUSE_BUTTON_1);
        if(mouse == 1) {
            DoubleBuffer xpos = BufferUtils.createDoubleBuffer(1);
            DoubleBuffer ypos = BufferUtils.createDoubleBuffer(1);
            GLFW.glfwGetCursorPos(this.windowHandle, xpos, ypos);

            this.localPoints.add(new int[] {(int)xpos.get(0), (int)ypos.get(0)});
            PacketDrawTest d = new PacketDrawTest((short) xpos.get(0), (short) ypos.get(0));
            d.timeSent = System.nanoTime();
            this.client.sendPacketUdp(d);
            System.out.println("Sent: " + d.x + ":" + d.y);
        }

        GL11.glPointSize(5f);

        GL11.glBegin(GL11.GL_POINTS);
        GL11.glColor3f(0, 1f, 0);
        for(int[] pn : this.localPoints) {
            GL11.glVertex2f(pn[0], pn[1]);
        }

        GL11.glColor3f(1f, 1f, 1f);
        for(short[] pn : this.points) {
            GL11.glVertex2f(pn[0], pn[1]);
        }
        GL11.glEnd();
    }

    private void tick() {
        Packet p;
        while((p = this.client.getProcessedPackets().poll()) != null) {
            PacketDrawTest d = ((PacketDrawTest)p);
            float time = (System.nanoTime() - d.timeSent) / 1000000000f;
            this.timePacket += time;
            this.num++;
            System.out.println("Time: " + time + " s");
            this.points.add(new short[] {d.x, d.y});
        }
    }

    private void fpsCount() {
        GLFW.glfwSetWindowTitle(this.windowHandle, "EpiCubes - " + this.fps + " FPS");
    }

    private void loopTick() {
        float ptt = (this.time - this.lastTime) / ((float) this.tickTimeSec);
        this.renderTick(ptt);

        this.fps++;

        this.time = System.nanoTime();
        while (time - lastTime >= this.tickTimeSec) {
            this.tick();

            lastTime += this.tickTimeSec;
        }

        if (time - lastInfo >= this.magicConstant) {
            lastInfo += this.magicConstant;
            this.fpsCount();
            lastTicks = ticks;
            fps = 0;
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
        System.out.println("Avg packet time: " + (this.timePacket / (this.num + 1)) + " ms");
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
            this.client = new ClientNetcode("127.0.0.1", 6927, 6928);
        } catch (IOException e) {
            e.printStackTrace();
        }

        GL11.glViewport(0, 0, width, height);

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, width, height, 0, 0, 1);
        GL11.glMatrixMode(GL11.GL_MODELVIEW);

        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
    }

    private void cleanup() {

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

    @Override
    public void run() {
        try {
            this.init();
            this.loop();
        } catch (Exception e) {
            System.out.println(e.getMessage());
            System.exit(this.shutdownCode);
        }
    }
}
