package cz.trigon.ecubes.client;

import cz.trigon.ecubes.exception.EpicubesException;
import cz.trigon.ecubes.exception.ExceptionHandling;
import cz.trigon.ecubes.exception.ExceptionUtil;
import cz.trigon.ecubes.log.EpiLogger;
import org.lwjgl.glfw.GLFW;
import org.lwjgl.glfw.GLFWErrorCallback;
import org.lwjgl.glfw.GLFWVidMode;
import org.lwjgl.opengl.GL;
import org.lwjgl.opengl.GL11;

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

    private void renderTick(float ptt) {

    }

    private void tick() {

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

        this.time = System.nanoTime();
        this.lastTime = time;
        this.lastInfo = time;

        while (GLFW.glfwWindowShouldClose(this.windowHandle) == GL11.GL_FALSE) {
            GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
            try {
                this.loopTick();
            } catch (EpicubesException e) {
                if (e.getUrgency() == EpicubesException.Urgency.BREAKING) {
                    throw e;
                } else if (e.getUrgency() == EpicubesException.Urgency.FEATURE_BREAKING) {
                    this.handleFeatureException(e);
                }
            } catch (Exception e) {
                throw ExceptionUtil.gameBreakingException(e, this, true);
            }
            GLFW.glfwSwapBuffers(this.windowHandle);
            GLFW.glfwPollEvents();
        }

        this.endMyLife();
    }

    private void handleFeatureException(EpicubesException e) {
        // TODO
    }

    @ExceptionHandling(value = "Window", errorCode = 150)
    private void init() {
        EpiLogger.init(this.getClass());

        GLFW.glfwSetErrorCallback(this.errorCallback = GLFWErrorCallback.createPrint(System.err));
        if (GLFW.glfwInit() != GL11.GL_TRUE) {
            throw ExceptionUtil.gameBreakingException("Couldn't init GLFW.", this, false);
        }

        GLFW.glfwDefaultWindowHints();
        GLFW.glfwWindowHint(GLFW.GLFW_RESIZABLE, GL11.GL_FALSE);
        GLFW.glfwWindowHint(GLFW.GLFW_SAMPLES, 4);

        this.windowHandle = GLFW.glfwCreateWindow(800, 600, "EpiCubes", 0, 0);

        if (this.windowHandle == 0) {
            throw ExceptionUtil.gameBreakingException("Couldn't create window.", this, false);
        }

        GLFW.glfwHideWindow(this.windowHandle);
        this.resize(800, 600);
        GLFW.glfwShowWindow(this.windowHandle);
    }

    private void fpsCount() {
        GLFW.glfwSetWindowTitle(this.windowHandle, "EpiCubes - " + this.fps + " FPS");
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

    public void shutdown(int code) {
        GLFW.glfwSetWindowShouldClose(this.windowHandle, GL11.GL_TRUE);
        this.shutdownCode = code;
    }

    @Override
    public void run() {
        try {
            this.init();
            this.loop();
        } catch (EpicubesException e) {
            this.shutdown(e.getErrorCode());
        }

        this.endMyLife();
    }

    private void endMyLife() {
        this.cleanup();
        if (this.shutdownCode == 0)
            EpiLogger.info("Shutting down nicely");
        else
            EpiLogger.warning("Shutting down with error code " + this.shutdownCode);

        System.exit(this.shutdownCode);
    }
}
