package cz.trigon.ecubes.log;

import cz.trigon.ecubes.exception.EpicubesException;

import java.util.logging.Level;
import java.util.logging.Logger;

public class EpiLogger {

    private static Logger jl;

    public static void exception(EpicubesException exception) {
        if(exception.getUrgency() == EpicubesException.Urgency.BREAKING)
            error(exception);
        else
            warning(exception);
    }

    public static void debug(String debug) {
        jl.log(Level.FINE, debug);
    }

    public static void debug(String debug, Object caller, String sourceMethod) {
        jl.logp(Level.FINE, caller.getClass().getName(), sourceMethod, debug);
    }

    public static void init(Class cl) {
        jl = Logger.getLogger(cl.getName());
    }

    public static void info(String info) {
        jl.log(Level.INFO, info);
    }

    public static void info(String info, Object caller, String sourceMethod) {
        jl.logp(Level.INFO, caller.getClass().getName(), sourceMethod, info);
    }

    public static void warning(String warning) {
        jl.log(Level.WARNING, warning);
    }

    public static void warning(String warning, Object caller, String sourceMethod) {
        jl.logp(Level.WARNING, caller.getClass().getName(), sourceMethod, warning);

    }

    public static void warning(EpicubesException exception) {
        warning("[" + exception.getFeatureBroken() + "] " + exception.getMessage(), exception.getSource(), null);
    }

    public static void error(String error) {
        jl.log(Level.SEVERE, error);
    }

    public static void error(String error, Object caller, String sourceMethod) {
        jl.logp(Level.SEVERE, caller.getClass().getName(), sourceMethod, error);
    }

    public static void error(EpicubesException exception) {
        error("[" + exception.getFeatureBroken() + "] " + exception.getMessage(), exception.getSource(), null);
    }

    public static void config(String changedSetting, String message) {
        jl.log(Level.CONFIG, message, changedSetting);
    }
}
