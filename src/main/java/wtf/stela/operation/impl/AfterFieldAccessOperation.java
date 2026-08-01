package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.AfterFieldAccess;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class AfterFieldAccessOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> handlers = source.methods.stream()
                .filter(m -> getAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode handler : handlers) {
            AfterFieldAccess info = getAnnotation(handler);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn instanceof FieldInsnNode) {
                    FieldInsnNode finsn = (FieldInsnNode) insn;
                    if (info.field().isEmpty() || finsn.name.equals(info.field())) {
                        InsnList patch = new InsnList();
                        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, handler.name, handler.desc, false));
                        targetMethod.instructions.insert(insn, patch);
                    }
                }
            }
        }
    }

    public static AfterFieldAccess getAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/AfterFieldAccess;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                String field = ASMUtil.getAnnotationValue(annotation, "field");
                return new AfterFieldAccess() {
                    @Override public Class<? extends Annotation> annotationType() { return AfterFieldAccess.class; }
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
