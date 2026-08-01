package wtf.stela.operation.impl;

import static wtf.stela.Stela.Logger;

import java.util.List;

import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class UniqueOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        if (source == null || target == null) return;

        // Copy unique fields
        if (source.fields != null) {
            for (FieldNode field : source.fields) {
                if (isUniqueField(field)) {
                    boolean exists = target.fields.stream().anyMatch(f -> f.name.equals(field.name));
                    if (!exists) {
                        FieldNode copiedField = new FieldNode(field.access, field.name, field.desc, field.signature, field.value);
                        target.fields.add(copiedField);
                        if (Logger != null) {
                            Logger.info("Added @Unique field {} ({}) to {}", field.name, field.desc, target.name);
                        }
                    }
                }
            }
        }

        // Copy unique methods
        if (source.methods != null) {
            for (MethodNode method : source.methods) {
                if (isUniqueMethod(method)) {
                    boolean exists = target.methods.stream().anyMatch(m -> m.name.equals(method.name) && m.desc.equals(method.desc));
                    if (!exists) {
                        MethodNode copiedMethod = new MethodNode(method.access, method.name, method.desc, method.signature, method.exceptions.toArray(new String[0]));
                        copiedMethod.instructions = OverwriteOperation.remapInsnList(method.instructions, source.name, target.name);
                        copiedMethod.localVariables = method.localVariables;
                        copiedMethod.tryCatchBlocks = method.tryCatchBlocks;
                        copiedMethod.maxLocals = method.maxLocals;
                        copiedMethod.maxStack = method.maxStack;

                        target.methods.add(copiedMethod);
                        if (Logger != null) {
                            Logger.info("Added @Unique method {}{} to {}", method.name, method.desc, target.name);
                        }
                    }
                }
            }
        }
    }

    private static boolean isUniqueField(FieldNode field) {
        if (field == null) return false;
        List<AnnotationNode> annotations = ASMUtil.getAnnotations(field);
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Unique;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/Unique;")) {
                return true;
            }
        }
        return false;
    }

    private static boolean isUniqueMethod(MethodNode method) {
        if (method == null) return false;
        if (method.name.equals("<init>") || method.name.equals("<clinit>")) return false;
        List<AnnotationNode> annotations = ASMUtil.getAnnotations(method);
        if (annotations == null) return false;
        for (AnnotationNode annotation : annotations) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Unique;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/Unique;")) {
                return true;
            }
        }
        return false;
    }
}
