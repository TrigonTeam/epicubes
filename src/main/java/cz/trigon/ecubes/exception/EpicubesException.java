package cz.trigon.ecubes.exception;

import cz.trigon.ecubes.log.EpiLogger;

import java.lang.reflect.Method;

public class EpicubesException extends RuntimeException {

    public enum Urgency {
        NON_BREAKING, BREAKING, FEATURE_BREAKING
    }

    private String feature;
    private Object source;
    private boolean serverSide;
    private Urgency urgency;
    private int errorCode;

    EpicubesException(String message, Throwable cause, Object source, boolean serverSide, boolean breaking, boolean featureBreaking) {
        super(message, cause, true, false);
        this.source = source;
        this.serverSide = serverSide;
        this.urgency = (breaking ? (featureBreaking ? Urgency.FEATURE_BREAKING : Urgency.BREAKING) : Urgency.NON_BREAKING);

        ExceptionHandling hat = source.getClass().getAnnotation(ExceptionHandling.class);

        if (hat != null) {
            this.feature = hat.value();
            this.errorCode = hat.errorCode();
            this.serverSide = hat.serverSide();
        }

        if (cause != null) {
            outer:
            for (StackTraceElement e : cause.getStackTrace()) {
                try {
                    for (Method m : Class.forName(e.getClassName()).getDeclaredMethods()) {
                        if(m.getName().equals(e.getMethodName())) {
                            ExceptionHandling mhat = m.getAnnotation(ExceptionHandling.class);

                            if (mhat != null) {
                                this.feature = mhat.value();
                                this.errorCode = mhat.errorCode();
                                this.serverSide = mhat.serverSide();
                                break outer;
                            }
                        }
                    }
                } catch (ClassNotFoundException e1) {
                    EpiLogger.warning("Error creating exception. RIP");
                }
            }
        }
    }

    public Object getSource() {
        return this.source;
    }

    public boolean isServerSide() {
        return this.serverSide;
    }

    public Urgency getUrgency() {
        return this.urgency;
    }

    public String getFeatureBroken() {
        return this.feature;
    }

    public int getErrorCode() {
        return this.errorCode;
    }
}
