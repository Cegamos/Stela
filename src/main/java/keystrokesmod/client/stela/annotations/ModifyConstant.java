package keystrokesmod.client.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
@java.lang.annotation.Target(ElementType.METHOD)
public @interface ModifyConstant {
    String[] method() default {};
    String desc() default "";
    Target target();
    boolean remap() default true;
}
