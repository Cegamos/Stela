package keystrokesmod.client.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target({ElementType.METHOD, ElementType.PARAMETER})
public @interface Cancellable {
    boolean value() default true;
}
