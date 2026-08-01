package wtf.stela.operation.impl;

import static wtf.stela.Stela.Logger;

import java.lang.annotation.Annotation;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.Overwrite;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

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
            Object targetName = info.method().length == 0 ? overwrite.name : info.method();
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
                Logger.error("Failed to find target method {} for overwrite in {}", Arrays.toString(info.method()), target.name);
            }
        }
    }

    public static InsnList remapInsnList(InsnList source, String sourceName, String targetName) {
        InsnList clone = new InsnList();
        Map<LabelNode, LabelNode> labelMap = new HashMap<>();
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode) {
                labelMap.put((LabelNode) insn, new LabelNode());
            }
        }
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            AbstractInsnNode cloned = insn.clone(labelMap);
            InjectOperation.remapInstruction(cloned, sourceName, targetName);
            clone.add(cloned);
        }
        return clone;
    }

    public static Overwrite getOverwriteAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Overwrite;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/Overwrite;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                if (nameList.isEmpty()) {
                    nameList.add(method.name);
                }
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                return new Overwrite() {
                    @Override public Class<? extends Annotation> annotationType() { return Overwrite.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
