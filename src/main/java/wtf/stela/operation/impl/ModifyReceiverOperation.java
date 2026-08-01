package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.ModifyReceiver;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class ModifyReceiverOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        if (source == null || target == null) return;

        List<MethodNode> modifiers = source.methods.stream()
                .filter(m -> getModifyReceiverAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifiers) {
            ModifyReceiver info = getModifyReceiverAnnotation(modifier);
            if (info == null) continue;

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            List<AbstractInsnNode> nodes = InjectOperation.findTargetInsns(targetMethod, info.target());
            for (AbstractInsnNode node : nodes) {
                if (!(node instanceof MethodInsnNode)) continue;
                MethodInsnNode call = (MethodInsnNode) node;
                if (call.getOpcode() == Opcodes.INVOKESTATIC) continue;

                Type[] argTypes = Type.getArgumentTypes(call.desc);
                List<Type> stack = new ArrayList<>();
                stack.add(Type.getObjectType(call.owner));
                for (Type argType : argTypes) stack.add(argType);

                ModifyArgOperation.replaceStackItem(targetMethod, node, stack, 0, source.name, modifier.name, modifier.desc);
            }
        }
    }

    public static ModifyReceiver getModifyReceiverAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/ModifyReceiver;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");

                String value = "INVOKEVIRTUAL";
                String target = "";
                int ordinal = 0;
                if (targetNode != null) {
                    String tv = ASMUtil.getAnnotationValue(targetNode, "value");
                    if (tv != null) value = tv;
                    String tt = ASMUtil.getAnnotationValue(targetNode, "target");
                    if (tt != null) target = tt;
                    Integer ord = ASMUtil.getAnnotationValue(targetNode, "ordinal");
                    if (ord != null) ordinal = ord;
                }

                final String fValue = value;
                final String fTarget = target;
                final int fOrdinal = ordinal;

                Target targetAnno = new Target() {
                    @Override public Class<? extends Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fValue; }
                    @Override public String target() { return fTarget; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return fOrdinal; }
                };

                return new ModifyReceiver() {
                    @Override public Class<? extends Annotation> annotationType() { return ModifyReceiver.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public Target target() { return targetAnno; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
