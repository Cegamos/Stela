package wtf.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.PARAMETER)
public @interface Local {
    String source() default "";
    String target() default "";
    int index() default -1;
    boolean argsOnly() default false;
}
