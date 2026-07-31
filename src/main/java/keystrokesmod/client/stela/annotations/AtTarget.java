package keystrokesmod.client.stela.annotations;

import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;

@Retention(RetentionPolicy.RUNTIME)
public @interface AtTarget {
    Target[] value();
}
