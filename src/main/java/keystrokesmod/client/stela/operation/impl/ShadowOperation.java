package keystrokesmod.client.stela.operation.impl;

import static keystrokesmod.client.stela.Stela.Logger;

import java.util.List;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

public class ShadowOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        if (source == null || target == null) return;

        // Process @Shadow fields
        if (source.fields != null) {
            for (FieldNode field : source.fields) {
                if (isShadowField(field)) {
                    FieldNode targetField = findTargetField(target.fields, field.name);
                    if (targetField != null) {
                        targetField.access = (targetField.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
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
                if (isShadowMethod(method)) {
                    MethodNode targetMethod = findTargetMethod(target.methods, method.name, method.desc);
                    if (targetMethod != null) {
                        targetMethod.access = (targetMethod.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC;
                        if (Logger != null) {
                            Logger.info("Exposed @Shadow method {}{} in {}", method.name, method.desc, target.name);
                        }
                    }
                }
            }
        }
    }

    private static boolean isShadowField(FieldNode field) {
        if (field == null) return false;
        List<AnnotationNode> annotations = ASMUtil.getAnnotations(field);
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/Shadow;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isShadowMethod(MethodNode method) {
        if (method == null) return false;
        List<AnnotationNode> annotations = ASMUtil.getAnnotations(method);
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/Shadow;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/Shadow;")) {
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
