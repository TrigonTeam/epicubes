package cz.trigon.ecubes.exception;

import cz.trigon.ecubes.log.EpiLogger;

public class ExceptionUtil {
    public static EpicubesException nonBreakingException(String message, Throwable original, Object source, boolean serverSide) {
        EpicubesException e = new EpicubesException(message, original, source, serverSide, false, false);
        EpiLogger.exception(e);
        return e;
    }

    public static EpicubesException featureBreakingException(String message, Throwable original, Object source, boolean serverSide) {
        EpicubesException e = new EpicubesException(message, original, source, serverSide, true, true);
        EpiLogger.exception(e);
        return e;
    }

    public static EpicubesException gameBreakingException(String message, Throwable original, Object source, boolean serverSide) {
        EpicubesException e = new EpicubesException(message, original, source, serverSide, true, false);
        EpiLogger.exception(e);
        return e;
    }

    public static EpicubesException nonBreakingException(Throwable original, Object source, boolean serverSide) {
        return ExceptionUtil.nonBreakingException(original.getMessage(), original, source, serverSide);
    }

    public static EpicubesException featureBreakingException(Throwable original, Object source, boolean serverSide) {
        return ExceptionUtil.featureBreakingException(original.getMessage(), original, source, serverSide);
    }

    public static EpicubesException gameBreakingException(Throwable original, Object source, boolean serverSide) {
        return ExceptionUtil.gameBreakingException(original.getMessage(), original, source, serverSide);
    }

    public static EpicubesException nonBreakingException(String message, Object source, boolean serverSide) {
        return ExceptionUtil.nonBreakingException(message, new Exception(), source, serverSide);
    }

    public static EpicubesException featureBreakingException(String message, Object source, boolean serverSide) {
        return ExceptionUtil.featureBreakingException(message, new Exception(), source, serverSide);
    }

    public static EpicubesException gameBreakingException(String message, Object source, boolean serverSide) {
        return ExceptionUtil.gameBreakingException(message, new Exception(), source, serverSide);
    }
}
