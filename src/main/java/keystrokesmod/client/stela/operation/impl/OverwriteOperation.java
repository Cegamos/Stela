package keystrokesmod.client.stela.operation.impl;

import static keystrokesmod.client.stela.Stela.Logger;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TypeInsnNode;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.Overwrite;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

public class OverwriteOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        List<MethodNode> overwrites = source.methods.stream()
                .filter(m -> getOverwriteAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode overwrite : overwrites) {
            Overwrite info = getOverwriteAnnotation(overwrite);
            if (info == null) continue;
            String targetName = info.method().isEmpty() ? overwrite.name : info.method();
            String targetDesc = info.desc().isEmpty() ? overwrite.desc : info.desc();

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), targetName, targetDesc);
            if (targetMethod != null) {
                targetMethod.instructions = remapInsnList(overwrite.instructions, source.name, target.name);
                targetMethod.localVariables = overwrite.localVariables;
                targetMethod.tryCatchBlocks = overwrite.tryCatchBlocks;
                targetMethod.maxLocals = overwrite.maxLocals;
                targetMethod.maxStack = overwrite.maxStack;
                if (Logger != null) {
                    Logger.info("Overwrote method {} in {}", targetMethod.name, target.name);
                }
            } else if (Logger != null) {
                Logger.error("Failed to find target method {} for overwrite in {}", targetName, target.name);
            }
        }
    }

    public static InsnList remapInsnList(InsnList source, String sourceName, String targetName) {
        InsnList clone = new InsnList();
        java.util.Map<LabelNode, LabelNode> labelMap = new java.util.HashMap<>();
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode) {
                labelMap.put((LabelNode) insn, new LabelNode());
            }
        }
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            AbstractInsnNode cloned = insn.clone(labelMap);
            if (cloned instanceof FieldInsnNode) {
                FieldInsnNode fieldInsn = (FieldInsnNode) cloned;
                if (fieldInsn.owner.equals(sourceName)) {
                    fieldInsn.owner = targetName;
                }
            } else if (cloned instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) cloned;
                if (methodInsn.owner.equals(sourceName)) {
                    methodInsn.owner = targetName;
                }
            } else if (cloned instanceof TypeInsnNode) {
                TypeInsnNode typeInsn = (TypeInsnNode) cloned;
                if (typeInsn.desc.equals(sourceName)) {
                    typeInsn.desc = targetName;
                }
            }
            clone.add(cloned);
        }
        return clone;
    }

    public static Overwrite getOverwriteAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/Overwrite;")) {
                String methodName = ASMUtil.getAnnotationValue(annotation, "method");
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                return new Overwrite() {
                    @Override public Class<? extends Annotation> annotationType() { return Overwrite.class; }
                    @Override public String method() { return methodName != null ? methodName : ""; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
