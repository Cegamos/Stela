package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.ModifyReturnValue;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

public class ModifyReturnValueOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> modifiers = source.methods.stream()
                .filter(m -> getModifyReturnValueAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifiers) {
            ModifyReturnValue info = getModifyReturnValueAnnotation(modifier);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                int opcode = insn.getOpcode();
                if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.ARETURN) {
                    InsnList patch = new InsnList();
                    patch.add(new MethodInsnNode(
                            Opcodes.INVOKESTATIC,
                            source.name,
                            modifier.name,
                            modifier.desc,
                            false
                    ));
                    targetMethod.instructions.insertBefore(insn, patch);
                }
            }
        }
    }

    public static ModifyReturnValue getModifyReturnValueAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : keystrokesmod.client.stela.util.ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/ModifyReturnValue;")) {
                String methodName = ASMUtil.getAnnotationValue(annotation, "method");
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                return new ModifyReturnValue() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return ModifyReturnValue.class; }
                    @Override public String method() { return methodName != null ? methodName : ""; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
