package wtf.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface BeforeFieldAccess {
    String[] method() default {};
    String desc() default "";
    String field();
    boolean remap() default true;
}
