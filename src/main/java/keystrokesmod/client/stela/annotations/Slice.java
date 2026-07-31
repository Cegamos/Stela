package keystrokesmod.client.stela.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface Slice {
    String id() default "";
    Target from() default @Target("HEAD");
    Target to() default @Target("TAIL");
}
