package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.Redirect;
import keystrokesmod.client.stela.annotations.Target;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.List;
import java.util.stream.Collectors;

public class RedirectOperation implements Operation {

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();

        List<MethodNode> redirects = source.methods.stream()
                .filter(m -> getRedirectAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode redirectMethod : redirects) {
            Redirect info = getRedirectAnnotation(redirectMethod);
            if (info == null) continue;
            MethodNode targetMethod = InjectOperation.findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) continue;

            Target tInfo = info.target();
            int opcode = getOpcode(tInfo.value());
            String targetSub = tInfo.target();

            for (AbstractInsnNode insn : targetMethod.instructions.toArray()) {
                if (insn.getOpcode() == opcode) {
                    if (insn instanceof MethodInsnNode) {
                        MethodInsnNode minsn = (MethodInsnNode) insn;
                        String full = minsn.owner + "." + minsn.name + minsn.desc;
                        if (targetSub.isEmpty() || full.contains(targetSub)) {
                            redirectMethod.access = (redirectMethod.access & ~Opcodes.ACC_PRIVATE & ~Opcodes.ACC_PROTECTED) | Opcodes.ACC_PUBLIC | Opcodes.ACC_STATIC;
                            targetMethod.instructions.set(insn, new MethodInsnNode(
                                    Opcodes.INVOKESTATIC,
                                    source.name,
                                    redirectMethod.name,
                                    redirectMethod.desc,
                                    false
                            ));
                            break;
                        }
                    }
                }
            }
        }
    }

    private int getOpcode(String val) {
        try {
            return Opcodes.class.getField(val).getInt(null);
        } catch (Exception ignored) {
            return Opcodes.INVOKEVIRTUAL;
        }
    }

    public static Redirect getRedirectAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : keystrokesmod.client.stela.util.ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/Redirect;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = InjectOperation.parseMethodNames(methodNameObj);
                final String[] methodArray = nameList.toArray(new String[0]);
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                
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
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fTargetValue; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return Shift.BEFORE; }
                    @Override public int ordinal() { return 0; }
                };

                return new Redirect() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Redirect.class; }
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
