package cz.trigon.ecubes.server;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {
        EpiServer s = new EpiServer();
        Thread t = new Thread(s);
        t.start();
    }
}
