package keystrokesmod.client.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface WrapWithTryCatch {
    String method();
    String desc() default "";
    Class<? extends Throwable> exception() default Throwable.class;
    Target target() default @Target("HEAD");
    boolean remap() default true;
}
