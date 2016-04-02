package cz.trigon.ecubes.exception;

import java.lang.annotation.Inherited;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Inherited
@Retention(RetentionPolicy.RUNTIME)
public @interface ExceptionHandling {
    String value();
    int errorCode() default 100;
    boolean serverSide() default true;
}
