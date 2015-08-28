package cz.trigon.ecubes.res;

import java.util.Locale;
import java.util.ResourceBundle;

public class Locales {
    private static Locales instance;

    public static Locales getInstance() {
        if(Locales.instance == null)
            Locales.instance = new Locales();

        return Locales.instance;
    }

    private ResourceBundle menuBundle;

    private Locales() {
        this.loadBundles(Locale.getDefault());
    }

    public void loadBundles(Locale locale) {
        this.menuBundle = ResourceBundle.getBundle("Menu", locale);
    }

    public ResourceBundle menu() {
        return this.menuBundle;
    }
}
