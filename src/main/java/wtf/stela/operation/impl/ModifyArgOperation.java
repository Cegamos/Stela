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
import wtf.stela.annotations.ModifyArg;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class ModifyArgOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        if (source == null || target == null) return;

        List<MethodNode> modifyArgs = source.methods.stream()
                .filter(m -> getModifyArgAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifyArgs) {
            ModifyArg info = getModifyArgAnnotation(modifier);
            if (info == null) continue;

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            if (info.target().value().equals("HEAD")) {
                modifyParamAtEntry(source, modifier, targetMethod, info.index());
            } else {
                modifyArgAtCallSite(source, modifier, targetMethod, info);
            }
        }
    }

    private static void modifyParamAtEntry(ClassNode source, MethodNode modifier, MethodNode targetMethod, int index) {
        Type[] argTypes = Type.getArgumentTypes(targetMethod.desc);
        if (index < 0 || index >= argTypes.length) return;

        Type paramType = argTypes[index];
        int offset = 0;
        for (int i = 0; i < index; i++) offset += argTypes[i].getSize();
        int slot = ((targetMethod.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1) + offset;

        AbstractInsnNode anchor = targetMethod.instructions.getFirst();
        if (anchor == null) return;

        InsnList patch = new InsnList();
        patch.add(new VarInsnNode(paramType.getOpcode(Opcodes.ILOAD), slot));
        patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC, source.name, modifier.name, modifier.desc, false));
        patch.add(new VarInsnNode(paramType.getOpcode(Opcodes.ISTORE), slot));

        targetMethod.instructions.insertBefore(anchor, patch);
    }

    private static void modifyArgAtCallSite(ClassNode source, MethodNode modifier, MethodNode targetMethod, ModifyArg info) {
        List<AbstractInsnNode> nodes = InjectOperation.findTargetInsns(targetMethod, info.target());
        for (AbstractInsnNode node : nodes) {
            if (!(node instanceof MethodInsnNode)) continue;
            MethodInsnNode call = (MethodInsnNode) node;

            Type[] argTypes = Type.getArgumentTypes(call.desc);
            boolean isStatic = call.getOpcode() == Opcodes.INVOKESTATIC;
            int index = info.index();
            if (index < 0 || index >= argTypes.length) continue;

            List<Type> stack = new java.util.ArrayList<>();
            if (!isStatic) {
                stack.add(Type.getObjectType(call.owner));
            }
            for (Type argType : argTypes) stack.add(argType);

            replaceStackItem(targetMethod, node, stack, isStatic ? index : index + 1, source.name, modifier.name, modifier.desc);
        }
    }

    public static void replaceStackItem(MethodNode targetMethod, AbstractInsnNode before, List<Type> stackBottomToTop, int replaceIndex, String owner, String handlerName, String handlerDesc) {
        if (replaceIndex < 0 || replaceIndex >= stackBottomToTop.size()) return;

        int count = stackBottomToTop.size();
        int tempBase = targetMethod.maxLocals;
        targetMethod.maxLocals = tempBase + count;

        InsnList patch = new InsnList();
        for (int i = count - 1; i >= 0; i--) {
            patch.add(new VarInsnNode(stackBottomToTop.get(i).getOpcode(Opcodes.ISTORE), tempBase + i));
        }
        for (int i = 0; i < count; i++) {
            patch.add(new VarInsnNode(stackBottomToTop.get(i).getOpcode(Opcodes.ILOAD), tempBase + i));
            if (i == replaceIndex) {
                patch.add(new MethodInsnNode(Opcodes.INVOKESTATIC, owner, handlerName, handlerDesc, false));
            }
        }
        targetMethod.instructions.insertBefore(before, patch);
    }

    public static ModifyArg getModifyArgAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/ModifyArg;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                Integer index = ASMUtil.getAnnotationValue(annotation, "index");

                String targetValue = "INVOKEVIRTUAL"; 
                String targetTarget = "";
                if (targetNode != null) {
                    String tv = ASMUtil.getAnnotationValue(targetNode, "value");
                    if (tv != null) targetValue = tv;

                    String tt = ASMUtil.getAnnotationValue(targetNode, "target");
                    if (tt != null) targetTarget = tt;
                }

                final String fTargetValue = targetValue;
                final String fTargetTarget = targetTarget;
                
                Target targetAnno = new Target() {
                    @Override public Class<? extends Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fTargetValue; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return 0; }
                };

                final int fIndex = index != null ? index : 0;

                return new ModifyArg() {
                    @Override public Class<? extends Annotation> annotationType() { return ModifyArg.class; }
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
