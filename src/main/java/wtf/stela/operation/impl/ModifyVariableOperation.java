package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import wtf.stela.Mixin;
import wtf.stela.Stela;
import wtf.stela.annotations.ModifyVariable;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class ModifyVariableOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        if (source == null || target == null) return;

        List<MethodNode> modifiers = source.methods.stream()
                .filter(m -> getModifyVariableAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifiers) {
            ModifyVariable info = getModifyVariableAnnotation(modifier);
            if (info == null) continue;

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            Type[] handlerArgs = Type.getArgumentTypes(modifier.desc);
            if (handlerArgs.length != 1) {
                if (Stela.Logger != null)
                    Stela.Logger.warn("ModifyVariable handler {} must take exactly 1 argument", modifier.name);
                continue;
            }

            int slot = resolveSlot(targetMethod, info.index());
            if (slot < 0) continue;

            List<AbstractInsnNode> nodes = InjectOperation.findTargetInsns(targetMethod, info.target());
            Type varType = handlerArgs[0];

            for (AbstractInsnNode node : nodes) {
                InsnList patch = new InsnList();
                patch.add(new VarInsnNode(varType.getOpcode(Opcodes.ILOAD), slot));
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, modifier.name, modifier.desc, false));
                patch.add(new VarInsnNode(varType.getOpcode(Opcodes.ISTORE), slot));
                targetMethod.instructions.insertBefore(node, patch);
            }
        }
    }

    private static int resolveSlot(MethodNode targetMethod, int index) {
        if (index >= 0) return index;

        Type[] argTypes = Type.getArgumentTypes(targetMethod.desc);
        int base = (targetMethod.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        int slot = base;
        for (Type argType : argTypes) slot += argType.getSize();
        return slot - 1;
    }

    public static ModifyVariable getModifyVariableAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/ModifyVariable;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/ModifyVariable;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                Integer index = ASMUtil.getAnnotationValue(annotation, "index");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                if (targetNode == null) {
                    Object atObj = ASMUtil.getAnnotationValue(annotation, "at");
                    if (atObj instanceof AnnotationNode) {
                        targetNode = (AnnotationNode) atObj;
                    } else if (atObj instanceof List && !((List<?>) atObj).isEmpty()) {
                        Object first = ((List<?>) atObj).get(0);
                        if (first instanceof AnnotationNode) {
                            targetNode = (AnnotationNode) first;
                        }
                    }
                }

                String value = "HEAD";
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

                final int fIndex = index != null ? index : -1;

                return new ModifyVariable() {
                    @Override public Class<? extends Annotation> annotationType() { return ModifyVariable.class; }
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
