package wtf.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface ModifyVariable {
    String[] method() default {};
    String desc() default "";
    Target target() default @Target("HEAD");
    int index() default -1;
    boolean remap() default true;
}
