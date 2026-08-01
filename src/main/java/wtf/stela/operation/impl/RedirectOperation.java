package wtf.stela.operation.impl;

import java.lang.annotation.Annotation;
import java.util.List;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.Redirect;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;

public class RedirectOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        if (source == null || target == null) return;

        List<MethodNode> redirects = source.methods.stream()
                .filter(m -> getRedirectAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode redirectMethod : redirects) {
            Redirect info = getRedirectAnnotation(redirectMethod);
            if (info == null) continue;

            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            String handlerDesc = redirectMethod.desc;
            List<AbstractInsnNode> nodes = InjectOperation.findTargetInsns(targetMethod, info.target());

            for (AbstractInsnNode node : nodes) {
                if (!(node instanceof MethodInsnNode) && !(node instanceof FieldInsnNode)) continue;
                targetMethod.instructions.set(node, new MethodInsnNode(
                        Opcodes.INVOKESTATIC,
                        source.name,
                        redirectMethod.name,
                        handlerDesc,
                        false
                ));
                redirectMethod.access = (redirectMethod.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
            }
        }
    }

    public static Redirect getRedirectAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Redirect;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/Redirect;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
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

                String targetValue = "INVOKEVIRTUAL";
                String targetTarget = "";
                int ordinal = 0;
                if (targetNode != null) {
                    String tv = ASMUtil.getAnnotationValue(targetNode, "value");
                    if (tv != null) targetValue = tv;
                    String tt = ASMUtil.getAnnotationValue(targetNode, "target");
                    if (tt != null) targetTarget = tt;
                    Integer ord = ASMUtil.getAnnotationValue(targetNode, "ordinal");
                    if (ord != null) ordinal = ord;
                }

                final String fTargetValue = targetValue;
                final String fTargetTarget = targetTarget;
                final int fOrdinal = ordinal;

                Target targetAnno = new Target() {
                    @Override public Class<? extends Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fTargetValue; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return fOrdinal; }
                };

                return new Redirect() {
                    @Override public Class<? extends Annotation> annotationType() { return Redirect.class; }
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
