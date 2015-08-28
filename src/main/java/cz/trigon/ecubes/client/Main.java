package cz.trigon.ecubes.client;

import cz.trigon.ecubes.res.ResourceLoader;

import java.io.IOException;

public class Main {
    public static void main(String[] args) throws IOException {/*
        SharedLibraryLoader.load();

        GameWindow window = new GameWindow();
        Thread t = new Thread(window);
        t.run();*/
        ResourceLoader l = new ResourceLoader();
        l.probe();
        l.getResourcesInDirectory("a/b").forEach(s -> System.out.println(s.getName()));
    }
}
