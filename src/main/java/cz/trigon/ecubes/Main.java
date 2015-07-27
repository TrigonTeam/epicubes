package cz.trigon.ecubes;

public class Main {
    public static void main(String[] args) {
        SharedLibraryLoader.load();

        GameWindow window = new GameWindow();
        Thread t = new Thread(window);
        t.run();
    }
}
