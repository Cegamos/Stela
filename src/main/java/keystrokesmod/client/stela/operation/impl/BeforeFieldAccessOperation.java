package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.BeforeFieldAccess;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

public class BeforeFieldAccessOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> handlers = source.methods.stream()
                .filter(m -> getAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode handler : handlers) {
            BeforeFieldAccess info = getAnnotation(handler);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode finsn = (FieldInsnNode) insn;
                    if (info.field().isEmpty() || finsn.name.equals(info.field())) {
                        InsnList patch = new InsnList();
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, handler.name, handler.desc, false));
                        targetMethod.instructions.insertBefore(insn, patch);
                    }
                }
            }
        }
    }

    public static BeforeFieldAccess getAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : keystrokesmod.client.stela.util.ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/BeforeFieldAccess;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                String field = ASMUtil.getAnnotationValue(annotation, "field");
                return new BeforeFieldAccess() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return BeforeFieldAccess.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public String field() { return field != null ? field : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
