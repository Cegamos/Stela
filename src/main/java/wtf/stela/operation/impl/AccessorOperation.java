package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.VarInsnNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.Accessor;
import wtf.stela.annotations.Invoker;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class AccessorOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> accessors = source.methods.stream()
                .filter(m -> getAccessorAnnotation(m) != null || getInvokerAnnotation(m) != null)
                .collect(Collectors.toList());

        if (accessors.isEmpty()) {
            return;
        }

        if ((source.access & Opcodes.ACC_INTERFACE) != 0) {
            if (!target.interfaces.contains(source.name)) {
                target.interfaces.add(source.name);
            }
        }

        for (MethodNode method : accessors) {
            Accessor accessor = getAccessorAnnotation(method);
            if (accessor != null) {
                String fieldName = accessor.value().isEmpty() ? method.name : accessor.value();
                boolean isGetter = method.desc.endsWith(")") || !method.desc.endsWith(")V");
                MethodNode generated = new MethodNode(Opcodes.ACC_PUBLIC, method.name, method.desc, null, null);
                InsnList insns = generated.instructions;
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));

                if (isGetter) {
                    FieldNode field = findField(target.fields, fieldName);
                    String fieldDesc = field != null ? field.desc : method.desc.substring(method.desc.lastIndexOf(')') + 1);
                    insns.add(new FieldInsnNode(Opcodes.GETFIELD, target.name, fieldName, fieldDesc));
                    insns.add(new InsnNode(getReturnOpcode(fieldDesc)));
                } else {
                    FieldNode field = findField(target.fields, fieldName);
                    String fieldDesc = field != null ? field.desc : getParamDesc(method.desc);
                    insns.add(new VarInsnNode(getLoadOpcode(fieldDesc), 1));
                    insns.add(new FieldInsnNode(Opcodes.PUTFIELD, target.name, fieldName, fieldDesc));
                    insns.add(new InsnNode(Opcodes.RETURN));
                }
                target.methods.add(generated);
                continue;
            }

            Invoker invoker = getInvokerAnnotation(method);
            if (invoker != null) {
                String targetMethodName = invoker.value().isEmpty() ? method.name : invoker.value();
                MethodNode generated = new MethodNode(Opcodes.ACC_PUBLIC, method.name, method.desc, null, null);
                InsnList insns = generated.instructions;
                insns.add(new VarInsnNode(Opcodes.ALOAD, 0));
                
                int varIndex = 1;
                Type[] argTypes = Type.getArgumentTypes(method.desc);
                for (Type argType : argTypes) {
                    insns.add(new VarInsnNode(argType.getOpcode(Opcodes.ILOAD), varIndex));
                    varIndex += argType.getSize();
                }
                
                insns.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, target.name, targetMethodName, method.desc, false));
                Type returnType = Type.getReturnType(method.desc);
                insns.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
                target.methods.add(generated);
            }
        }
    }

    private FieldNode findField(List<FieldNode> fields, String name) {
        for (FieldNode field : fields) {
            if (field.name.equals(name)) return field;
        }
        return null;
    }

    private String getParamDesc(String methodDesc) {
        Type[] types = Type.getArgumentTypes(methodDesc);
        return types.length > 0 ? types[0].getDescriptor() : "Ljava/lang/Object;";
    }

    private int getReturnOpcode(String desc) {
        return Type.getType(desc).getOpcode(Opcodes.IRETURN);
    }

    private int getLoadOpcode(String desc) {
        return Type.getType(desc).getOpcode(Opcodes.ILOAD);
    }

    public static Accessor getAccessorAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Accessor;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/gen/Accessor;")) {
                String val = ASMUtil.getAnnotationValue(annotation, "value");
                return new Accessor() {
                    @Override public Class<? extends Annotation> annotationType() { return Accessor.class; }
                    @Override public String value() { return val != null ? val : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }

    public static Invoker getInvokerAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Invoker;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/gen/Invoker;")) {
                String val = ASMUtil.getAnnotationValue(annotation, "value");
                return new Invoker() {
                    @Override public Class<? extends Annotation> annotationType() { return Invoker.class; }
                    @Override public String value() { return val != null ? val : ""; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }
}
