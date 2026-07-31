package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.ModifyConstant;
import keystrokesmod.client.stela.annotations.Target;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

public class ModifyConstantOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> modifyConstants = source.methods.stream()
                .filter(m -> getModifyConstantAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifyConstants) {
            ModifyConstant info = getModifyConstantAnnotation(modifier);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            int targetOrdinal = info.target().ordinal();
            int currentOrdinal = 0;

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn instanceof LdcInsnNode || (insn.getOpcode() >= Opcodes.ICONST_M1 && insn.getOpcode() <= Opcodes.DCONST_1)) {
                    if (currentOrdinal == targetOrdinal) {
                        targetMethod.instructions.set(insn, new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                source.name,
                                modifier.name,
                                modifier.desc,
                                false
                        ));
                        break;
                    }
                    currentOrdinal++;
                }
            }
        }
    }

    public static ModifyConstant getModifyConstantAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : keystrokesmod.client.stela.util.ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/ModifyConstant;")) {
                String methodName = ASMUtil.getAnnotationValue(annotation, "method");
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");

                int targetOrdinal = 0;
                if (targetNode != null) {
                    Integer ord = ASMUtil.getAnnotationValue(targetNode, "ordinal");
                    if (ord != null) targetOrdinal = ord;
                }

                final int fOrdinal = targetOrdinal;
                Target targetAnno = new Target() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return "LDC"; }
                    @Override public String target() { return ""; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return fOrdinal; }
                };

                return new ModifyConstant() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return ModifyConstant.class; }
                    @Override public String method() { return methodName != null ? methodName : ""; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public Target target() { return targetAnno; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
