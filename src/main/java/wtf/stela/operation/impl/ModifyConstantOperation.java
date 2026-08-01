package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.LdcInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.ModifyConstant;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class ModifyConstantOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> modifyConstants = source.methods.stream()
                .filter(m -> getModifyConstantAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode modifier : modifyConstants) {
            ModifyConstant info = getModifyConstantAnnotation(modifier);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            int targetOrdinal = info.target().ordinal();
            int currentOrdinal = 0;

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn instanceof LdcInsnNode || (insn.getOpcode() >= Opcodes.ICONST_M1 && insn.getOpcode() <= Opcodes.DCONST_1)) {
                    if (currentOrdinal == targetOrdinal) {
                        InsnList patch = new InsnList();
                        patch.add(new MethodInsnNode(
                                Opcodes.INVOKESTATIC,
                                source.name,
                                modifier.name,
                                modifier.desc,
                                false
                        ));
                        targetMethod.instructions.insert(insn, patch);
                        break;
                    }
                    currentOrdinal++;
                }
            }
        }
    }

    public static ModifyConstant getModifyConstantAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/ModifyConstant;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/ModifyConstant;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                if (targetNode == null) {
                    Object constObj = ASMUtil.getAnnotationValue(annotation, "constant");
                    if (constObj instanceof AnnotationNode) {
                        targetNode = (AnnotationNode) constObj;
                    } else if (constObj instanceof List && !((List<?>) constObj).isEmpty()) {
                        Object first = ((List<?>) constObj).get(0);
                        if (first instanceof AnnotationNode) {
                            targetNode = (AnnotationNode) first;
                        }
                    }
                }

                int targetOrdinal = 0;
                if (targetNode != null) {
                    Integer ord = ASMUtil.getAnnotationValue(targetNode, "ordinal");
                    if (ord != null) targetOrdinal = ord;
                }

                final int fOrdinal = targetOrdinal;
                Target targetAnno = new Target() {
                    @Override public Class<? extends Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return "LDC"; }
                    @Override public String target() { return ""; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return fOrdinal; }
                };

                return new ModifyConstant() {
                    @Override public Class<? extends Annotation> annotationType() { return ModifyConstant.class; }
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
