package wtf.stela.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Target {
    enum Shift {
        BEFORE, AFTER
    }

    String value();
    String target() default "";
    Shift shift() default Shift.BEFORE;
    int ordinal() default 0;
}
