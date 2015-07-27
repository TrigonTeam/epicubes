package cz.trigon.ecubes;

import org.lwjgl.Sys;
import org.lwjgl.glfw.Callbacks;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWvidmode;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.nio.ByteBuffer;

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

    private void renderTick(float ptt) {

    }

    private void tick() {

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
        this.time = System.nanoTime();
        this.lastTime = time;
        this.lastInfo = time;

        while (GLFW.glfwWindowShouldClose(this.windowHandle) == GL11.GL_FALSE) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT);
            this.loopTick();
            GLFW.glfwSwapBuffers(this.windowHandle);
            GLFW.glfwPollEvents();
        }

        this.cleanup();
        System.exit(this.shutdownCode);
    }

    private void init() {
        GLFW.glfwSetErrorCallback(this.errorCallback = Callbacks
                .errorCallbackPrint(System.err));

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

    private void cleanup() {

    }

    public void resize(int width, int height) {
        this.width = width;
        this.height = height;

        ByteBuffer vidmode = GLFW
                .glfwGetVideoMode(GLFW.glfwGetPrimaryMonitor());

        GLFW.glfwSetWindowPos(this.windowHandle,
                (GLFWvidmode.width(vidmode) - width) / 2,
                (GLFWvidmode.height(vidmode) - height) / 2);

        GLFW.glfwMakeContextCurrent(this.windowHandle);
        GLFW.glfwSwapInterval(0);
        GLContext.createFromCurrent();

        GL11.glViewport(0, 0, width, height);
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
