package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.InjectIf;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class InjectIfOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> injections = source.methods.stream()
                .filter(m -> getInjectIfAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode injection : injections) {
            InjectIf info = getInjectIfAnnotation(injection);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            LabelNode skipLabel = new LabelNode();
            InsnList wrapper = new InsnList();
            wrapper.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, info.condition(), "()Z", false));
            wrapper.add(new JumpInsnNode(Opcodes.IFEQ, skipLabel));
            wrapper.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, injection.name, injection.desc, false));
            wrapper.add(skipLabel);

            targetMethod.instructions.insert(wrapper);
        }
    }

    public static InjectIf getInjectIfAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/InjectIf;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                String condition = ASMUtil.getAnnotationValue(annotation, "condition");
                return new InjectIf() {
                    @Override public Class<? extends Annotation> annotationType() { return InjectIf.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public String condition() { return condition != null ? condition : ""; }
                    @Override public wtf.stela.annotations.Target target() { return null; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
