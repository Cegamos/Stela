package wtf.stela.operation.impl;

import static wtf.stela.Stela.Logger;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.operation.Operation;

public class ShadowOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        if (source == null || target == null) return;

        if (source.fields != null) {
            for (FieldNode field : source.fields) {
                if (isShadow(field.visibleAnnotations)) {
                    FieldNode targetField = findTargetField(target.fields, field.name);
                    if (targetField != null) {
                        targetField.access = (targetField.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
                        if (hasAnnotation(field.visibleAnnotations, "Mutable")) {
                            targetField.access &= ~Opcodes.ACC_FINAL;
                            if (Logger != null) {
                                Logger.info("@Mutable @Shadow field {} in {} (removed final)", field.name, target.name);
                            }
                        }
                        if (hasAnnotation(field.visibleAnnotations, "Final") && (targetField.access & Opcodes.ACC_FINAL) == 0) {
                            if (Logger != null) {
                                Logger.warn("@Final @Shadow field {} in {} is not final!", field.name, target.name);
                            }
                        }
                        if (Logger != null) {
                            Logger.info("Exposed @Shadow field {} in {}", field.name, target.name);
                        }
                    }
                }
            }
        }

        // Process @Shadow methods
        if (source.methods != null) {
            for (MethodNode method : source.methods) {
                if (isShadow(method.visibleAnnotations)) {
                    MethodNode targetMethod = findTargetMethod(target.methods, method.name, method.desc);
                    if (targetMethod != null) {
                        targetMethod.access = (targetMethod.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
                        if (hasAnnotation(method.visibleAnnotations, "Mutable")) {
                            targetMethod.access &= ~Opcodes.ACC_FINAL;
                        }
                        if (hasAnnotation(method.visibleAnnotations, "Final") && (targetMethod.access & Opcodes.ACC_FINAL) == 0) {
                            if (Logger != null) {
                                Logger.warn("@Final @Shadow method {}{} in {} is not final!", method.name, method.desc, target.name);
                            }
                        }
                        if (Logger != null) {
                            Logger.info("Exposed @Shadow method {}{} in {}", method.name, method.desc, target.name);
                        }
                    }
                }
            }
        }
    }

    private static boolean isShadow(List<AnnotationNode> annotations) {
        return hasAnnotation(annotations, "Shadow");
    }

    private static boolean hasAnnotation(List<AnnotationNode> annotations, String name) {
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lwtf/stela/annotations/" + name + ";") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/" + name + ";")) {
                return true;
            }
        }
        return false;
    }

    private static FieldNode findTargetField(List<FieldNode> fields, String name) {
        if (fields == null || name == null) return null;
        for (FieldNode field : fields) {
            if (field.name.equals(name)) return field;
        }
        return null;
    }

    private static MethodNode findTargetMethod(List<MethodNode> methods, String name, String desc) {
        if (methods == null || name == null) return null;
        for (MethodNode method : methods) {
            if (method.name.equals(name) && (desc.isEmpty() || method.desc.equals(desc))) {
                return method;
            }
        }
        return null;
    }
}
