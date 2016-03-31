package cz.trigon.ecubes.client;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException, InterruptedException {
        GameWindow window = new GameWindow();
        Thread t = new Thread(window);
        t.run();
    }
}
