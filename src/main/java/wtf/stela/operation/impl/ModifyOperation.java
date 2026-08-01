package wtf.stela.operation.impl;

import static wtf.stela.Stela.Logger;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.Modify;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class ModifyOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        if (source == null || target == null) return;

        List<MethodNode> modifiers = source.methods.stream()
                .filter(m -> getModifyAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifiers) {
            Modify info = getModifyAnnotation(modifier);
            if (info == null) continue;

            String targetMethodName = info.method().isEmpty() ? modifier.name : info.method();
            String targetDesc = info.desc().isEmpty() ? modifier.desc : info.desc();

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), targetMethodName, targetDesc);
            if (targetMethod == null) {
                if (Logger != null) {
                    Logger.error("Modify: no target method {} in {}", targetMethodName + targetDesc, target.name);
                }
                continue;
            }

            boolean isStatic = (targetMethod.access & Opcodes.ACC_STATIC) != 0;
            Type returnType = Type.getReturnType(targetMethod.desc);
            Type[] argTypes = Type.getArgumentTypes(targetMethod.desc);

            String handlerDesc;
            if (isStatic) {
                handlerDesc = targetMethod.desc;
            } else {
                StringBuilder sb = new StringBuilder("(");
                sb.append("L").append(target.name).append(";");
                for (Type argType : argTypes) sb.append(argType.getDescriptor());
                sb.append(returnType.getDescriptor());
                handlerDesc = sb.toString();
            }

            InsnList delegator = new InsnList();
            int varIndex = 0;
            if (!isStatic) {
                delegator.add(new VarInsnNode(Opcodes.ALOAD, 0));
                varIndex = 1;
            }
            for (Type argType : argTypes) {
                delegator.add(new VarInsnNode(argType.getOpcode(Opcodes.ILOAD), varIndex));
                varIndex += argType.getSize();
            }
            delegator.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, modifier.name, handlerDesc, false));
            delegator.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));

            targetMethod.instructions = delegator;
            targetMethod.tryCatchBlocks = null;
            targetMethod.localVariables = null;

            if (Logger != null) {
                Logger.info("Modify: delegated {}{} in {} to {} in {}", targetMethod.name, targetMethod.desc, target.name, modifier.name, source.name);
            }
        }
    }

    public static Modify getModifyAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Modify;")) {
                String m = ASMUtil.getAnnotationValue(annotation, "method");
                String d = ASMUtil.getAnnotationValue(annotation, "desc");
                final String fm = m != null ? m : "";
                final String fd = d != null ? d : "";
                return new Modify() {
                    @Override public Class<? extends Annotation> annotationType() { return Modify.class; }
                    @Override public String method() { return fm; }
                    @Override public String desc() { return fd; }
                };
            }
        }
        return null;
    }
}
