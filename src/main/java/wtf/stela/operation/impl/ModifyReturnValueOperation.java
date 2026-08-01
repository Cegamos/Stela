package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.ModifyReturnValue;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

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
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/ModifyReturnValue;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                return new ModifyReturnValue() {
                    @Override public Class<? extends Annotation> annotationType() { return ModifyReturnValue.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
