package keystrokesmod.client.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface Inject {
    String method();
    String desc() default "";
    Target target() default @Target("HEAD");
    boolean cancellable() default false;
    boolean remap() default true;
}
