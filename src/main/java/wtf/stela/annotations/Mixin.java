package wtf.stela.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;
import java.util.ArrayList;
import java.util.List;

import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;

@Retention(RetentionPolicy.RUNTIME)
@Target(ElementType.TYPE)
public @interface Mixin {
    Class<?> value() default Object.class;
    String name() default "";

    class Info {
        public final String targetClassName;
        public final String name;

        public Info(String targetClassName, String name) {
            this.targetClassName = targetClassName;
            this.name = name;
        }

        public static Info getInfo(ClassNode node) {
            if (node == null) return null;
            List<AnnotationNode> annotations = new ArrayList<>();
            if (node.visibleAnnotations != null) annotations.addAll(node.visibleAnnotations);
            if (node.invisibleAnnotations != null) annotations.addAll(node.invisibleAnnotations);

            for (AnnotationNode annotation : annotations) {
                if (annotation.desc.equals("Lwtf/stela/annotations/Mixin;") ||
                    annotation.desc.equals("Lorg/spongepowered/asm/mixin/Mixin;")) {
                    String targetClassName = "java.lang.Object";
                    String name = "";

                    if (annotation.values != null) {
                        for (int i = 0; i < annotation.values.size(); i += 2) {
                            String key = (String) annotation.values.get(i);
                            Object val = annotation.values.get(i + 1);
                            if (key.equals("value")) {
                                if (val instanceof Type) {
                                    targetClassName = ((Type) val).getClassName();
                                } else if (val instanceof List && !((List<?>) val).isEmpty()) {
                                    Object first = ((List<?>) val).get(0);
                                    if (first instanceof Type) {
                                        targetClassName = ((Type) first).getClassName();
                                    }
                                }
                            } else if (key.equals("name")) {
                                if (val instanceof String) {
                                    name = (String) val;
                                }
                            } else if (key.equals("targets")) {
                                if (val instanceof String) {
                                    targetClassName = (String) val;
                                } else if (val instanceof List && !((List<?>) val).isEmpty()) {
                                    Object first = ((List<?>) val).get(0);
                                    if (first instanceof String) {
                                        targetClassName = (String) first;
                                    }
                                }
                            }
                        }
                    }

                    if (name != null && !name.trim().isEmpty()) {
                        targetClassName = name.trim();
                    }

                    return new Info(targetClassName, name);
                }
            }
            return null;
        }
    }
}
