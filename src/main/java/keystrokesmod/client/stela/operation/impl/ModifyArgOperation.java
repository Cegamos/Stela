package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.ModifyArg;
import keystrokesmod.client.stela.annotations.Target;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

public class ModifyArgOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> modifyArgs = source.methods.stream()
                .filter(m -> getModifyArgAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifyArgs) {
            ModifyArg info = getModifyArgAnnotation(modifier);
            if (info == null) continue;

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            // Clone and inline modifier into target class as a private static method to prevent cross-package IllegalAccessError
            String syntheticName = "stela_modifyArg_" + modifier.name + "_" + Math.abs(modifier.name.hashCode());
            MethodNode copiedModifier = new MethodNode(
                    Opcodes.ACC_PRIVATE | Opcodes.ACC_STATIC | Opcodes.ACC_SYNTHETIC,
                    syntheticName,
                    modifier.desc,
                    modifier.signature,
                    modifier.exceptions != null ? modifier.exceptions.toArray(new String[0]) : null
            );

            InsnList copiedInsns = OverwriteOperation.remapInsnList(modifier.instructions, source.name, target.name);
            copiedModifier.instructions.add(copiedInsns);
            copiedModifier.tryCatchBlocks = modifier.tryCatchBlocks;
            copiedModifier.maxStack = modifier.maxStack;
            copiedModifier.maxLocals = modifier.maxLocals;

            target.methods.add(copiedModifier);

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn instanceof MethodInsnNode) {
                    MethodInsnNode minsn = (MethodInsnNode) insn;
                    if (info.target().target().isEmpty() || (minsn.owner + "." + minsn.name).contains(info.target().target())) {
                        InsnList patch = new InsnList();
                        patch.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                target.name,
                                syntheticName,
                                modifier.desc,
                                false
                        ));
                        targetMethod.instructions.insertBefore(insn, patch);
                        break;
                    }
                }
            }
        }
    }

    public static ModifyArg getModifyArgAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : keystrokesmod.client.stela.util.ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/ModifyArg;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                Integer index = ASMUtil.getAnnotationValue(annotation, "index");

                String targetTarget = "";
                if (targetNode != null) {
                    String tt = ASMUtil.getAnnotationValue(targetNode, "target");
                    if (tt != null) targetTarget = tt;
                }

                final String fTargetTarget = targetTarget;
                Target targetAnno = new Target() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return "INVOKEVIRTUAL"; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return 0; }
                };

                final int fIndex = index != null ? index : 0;

                return new ModifyArg() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return ModifyArg.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public Target target() { return targetAnno; }
                    @Override public int index() { return fIndex; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
