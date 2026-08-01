package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Local;
import keystrokesmod.client.stela.annotations.Target;
import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.util.ASMUtil;
import keystrokesmod.client.stela.util.DescParser;
import keystrokesmod.client.stela.util.Mapper;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.tree.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static keystrokesmod.client.stela.Stela.Logger;

public class InjectOperation implements Operation {
    
    private static int getLocalVarIndex(MethodNode node, String name) {
        try {
            return Integer.parseInt(name);
        } catch (Exception ignored) {}
        
        if (node.localVariables != null) {
            for (LocalVariableNode localVar : node.localVariables) {
                if (localVar.name.equals(name)) {
                    return localVar.index;
                }
            }
        }
        return -1;
    }

    private static ArrayList<String[]> getLocalParameters(MethodNode node) {
        ArrayList<String[]> parameters = new ArrayList<>();
        if (node.visibleParameterAnnotations == null) return parameters;
        for (List<AnnotationNode> visibleParameterAnnotation : node.visibleParameterAnnotations) {
            if (visibleParameterAnnotation == null) continue;
            for (AnnotationNode annotationNode : visibleParameterAnnotation) {
                if (annotationNode != null && annotationNode.desc.contains(ASMUtil.slash(Local.class.getName()))) {
                    String sourceParam = ASMUtil.getAnnotationValue(annotationNode, "source");
                    String targetVar = ASMUtil.getAnnotationValue(annotationNode, "target");
                    Integer indexVal = ASMUtil.getAnnotationValue(annotationNode, "index");
                    int index = indexVal != null ? indexVal : -1;
                    parameters.add(new String[]{sourceParam != null ? sourceParam : "", index != -1 ? String.valueOf(index) : (targetVar != null ? targetVar : "")});
                }
            }
        }
        return parameters;
    }

    private static boolean isLoadOpe(int opcode) {
        return opcode >= Opcodes.ILOAD && opcode <= Opcodes.ALOAD;
    }

    private static boolean isStoreOpe(int opcode) {
        return opcode >= Opcodes.ISTORE && opcode <= Opcodes.ASTORE;
    }

    private static void processLocalValues(MethodNode source, MethodNode target) {
        int shiftAmount = target.maxLocals;

        Map<Integer, Integer> varMap = new HashMap<>();
        ArrayList<String[]> sourceParameters = getLocalParameters(source);
        
        for (int i = 0; i < source.instructions.size(); i++) {
            AbstractInsnNode instruction = source.instructions.get(i);
            if (instruction instanceof VarInsnNode && isStoreOpe(instruction.getOpcode())) {
                VarInsnNode varInsnNode = (VarInsnNode) instruction;
                boolean canChange = true;
                for (String[] sourceParameter : sourceParameters) {
                    if (getLocalVarIndex(source, sourceParameter[0]) == varInsnNode.var) {
                        canChange = false;
                        break;
                    }
                }
                if (canChange && !varMap.containsKey(varInsnNode.var)) {
                    varMap.put(varInsnNode.var, varInsnNode.var + shiftAmount);
                }
            }
        }
        
        for (String[] sourceParameter : sourceParameters) {
            int sourceIndex = getLocalVarIndex(source, sourceParameter[0]);
            int targetIndex = getLocalVarIndex(target, sourceParameter[1]);
            if (sourceIndex != -1 && targetIndex != -1) {
                varMap.put(sourceIndex, targetIndex);
            }
        }
        
        int newMaxLocals = target.maxLocals;
        for (int i = 0; i < source.instructions.size(); i++) {
            AbstractInsnNode instruction = source.instructions.get(i);
            if (instruction instanceof VarInsnNode && (isLoadOpe(instruction.getOpcode()) || isStoreOpe(instruction.getOpcode()))) {
                VarInsnNode varInsnNode = (VarInsnNode) instruction;
                Integer newIndex = varMap.get(varInsnNode.var);
                if (newIndex != null) {
                    varInsnNode.var = newIndex;
                    newMaxLocals = Math.max(newMaxLocals, newIndex + 2);
                }
            } else if (instruction instanceof IincInsnNode) {
                IincInsnNode iincInsnNode = (IincInsnNode) instruction;
                Integer newIndex = varMap.get(iincInsnNode.var);
                if (newIndex != null) {
                    iincInsnNode.var = newIndex;
                    newMaxLocals = Math.max(newMaxLocals, newIndex + 1);
                }
            }
        }
        
        target.maxLocals = newMaxLocals;
    }

    @Override
    public void dispose(Mixin mixin) {
        ClassNode source = mixin.getSource();
        ClassNode target = mixin.getTarget();
        List<MethodNode> injections = source.methods.stream()
                .filter(m -> getInjectAnnotation(m) != null)
                .collect(Collectors.toList());
                
        for (MethodNode injection : injections) {
            Inject info = getInjectAnnotation(injection);
            if (info == null) continue;
            
            MethodNode targetMethod = findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) {
                if (Logger != null)
                    Logger.error("No method found: {} in {}", info.method() + info.desc(), target.name);
                continue;
            }
            
            processLocalValues(injection, targetMethod);
            
            try {
                insert(source.name, target.name, injection, targetMethod, info);
            } catch (Throwable e) {
                if (Logger != null) Logger.exception(e);
            }
        }
    }

    public static Inject getInjectAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lkeystrokesmod/client/stela/annotations/Inject;")) {
                String methodName = ASMUtil.getAnnotationValue(annotation, "method");
                String desc = ASMUtil.getAnnotationValue(annotation, "desc");
                AnnotationNode targetNode = ASMUtil.getAnnotationValue(annotation, "target");
                
                String targetValue = "HEAD";
                String targetTarget = "";
                Target.Shift targetShift = Target.Shift.BEFORE;
                int targetOrdinal = 0;
                
                if (targetNode != null) {
                    String tv = ASMUtil.getAnnotationValue(targetNode, "value");
                    if (tv != null) targetValue = tv;
                    String tt = ASMUtil.getAnnotationValue(targetNode, "target");
                    if (tt != null) targetTarget = tt;
                    String[] ts = ASMUtil.getAnnotationValue(targetNode, "shift");
                    if (ts != null && ts.length > 1) {
                        try { targetShift = Target.Shift.valueOf(ts[1]); } catch (Exception ignored) {}
                    }
                    Integer ord = ASMUtil.getAnnotationValue(targetNode, "ordinal");
                    if (ord != null) targetOrdinal = ord;
                }

                final String fTargetValue = targetValue;
                final String fTargetTarget = targetTarget;
                final Target.Shift fTargetShift = targetShift;
                final int fTargetOrdinal = targetOrdinal;

                Target targetAnno = new Target() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fTargetValue; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return fTargetShift; }
                    @Override public int ordinal() { return fTargetOrdinal; }
                };

                return new Inject() {
                    @Override public Class<? extends java.lang.annotation.Annotation> annotationType() { return Inject.class; }
                    @Override public String method() { return methodName != null ? methodName : ""; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public Target target() { return targetAnno; }
                    @Override public boolean cancellable() { return false; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }

    public static MethodNode findTargetMethod(List<MethodNode> methods, String targetOwner, String methodName, String desc) {
        for (MethodNode method : methods) {
            if (method.name.equals(methodName) && (desc == null || desc.isEmpty() || method.desc.equals(desc))) {
                return method;
            }
        }
        return null;
    }

    private static int getOperationCode(String ope) {
        try {
            return (int) Opcodes.class.getField(ope).get(null);
        } catch (Exception ignored) {}
        return -1;
    }

    private static String getMethodInsnNodeOperation(AbstractInsnNode node) {
        if (node instanceof MethodInsnNode) {
            MethodInsnNode mNode = (MethodInsnNode) node;
            return mNode.owner + "." + mNode.name + mNode.desc;
        }
        return null;
    }

    private static String getFieldInsnNodeOperation(AbstractInsnNode node) {
        if (node instanceof FieldInsnNode) {
            FieldInsnNode fNode = (FieldInsnNode) node;
            return fNode.owner + "." + fNode.name + " " + fNode.desc;
        }
        return null;
    }

    private static String[] parseOpe(String ope) {
        if (ope == null || ope.isEmpty()) {
            return new String[]{"", "", ""};
        }
        if (!ope.contains(".")) {
            return new String[]{"", ope, ""};
        }
        
        int lastDot = ope.lastIndexOf('.');
        String owner = ope.substring(0, lastDot);
        String nameAndDesc = ope.substring(lastDot + 1);
        
        String name;
        String desc;
        if (nameAndDesc.contains("(")) {
            int openParen = nameAndDesc.indexOf('(');
            name = nameAndDesc.substring(0, openParen);
            desc = nameAndDesc.substring(openParen);
        } else if (nameAndDesc.contains(" ")) {
            int space = nameAndDesc.indexOf(' ');
            name = nameAndDesc.substring(0, space);
            desc = nameAndDesc.substring(space + 1);
        } else {
            name = nameAndDesc;
            desc = "";
        }
        
        return new String[]{owner, name, desc};
    }

    private static String mapOperation(String ope) {
        if (ope == null || ope.isEmpty()) {
            return "";
        }
        if (!ope.contains(".")) {
            return ope;
        }
        boolean isMethod = !ope.contains(" ");
        String[] values = parseOpe(ope);
        String[] res = new String[3];
        res[0] = values[0].isEmpty() ? "" : Mapper.map(null, values[0], null, Mapper.Type.Class);
        res[1] = isMethod ? Mapper.mapMethodWithSuper(values[0], values[1], values[2]) : Mapper.mapFieldWithSuper(values[0], values[1], values[2]);
        res[2] = DescParser.mapDesc(values[2]);
        return (res[0].isEmpty() ? "" : res[0] + ".") + res[1] + (isMethod ? "" : " ") + res[2];
    }

    private static List<AbstractInsnNode> findTargetInsnNodes(MethodNode target, Inject info) {
        List<AbstractInsnNode> nodes = new ArrayList<>();
        Target targetInfo = info.target();

        if (targetInfo.value().equals("HEAD")) {
            for (AbstractInsnNode insn : target.instructions.toArray()) {
                if (insn.getOpcode() >= 0) {
                    nodes.add(insn);
                    return nodes;
                }
            }
            if (target.instructions.getFirst() != null) {
                nodes.add(target.instructions.getFirst());
            }
            return nodes;
        }

        if (targetInfo.value().equals("TAIL")) {
            int index = 0;
            for (AbstractInsnNode instruction : target.instructions.toArray()) {
                int opcode = instruction.getOpcode();
                if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                    if (targetInfo.ordinal() == 0 || targetInfo.ordinal() == index) {
                        nodes.add(instruction);
                    }
                    index++;
                }
            }
            return nodes;
        }

        String targetOpe = targetInfo.target().isEmpty() ? "" : mapOperation(targetInfo.target());
        int opcode = getOperationCode(targetInfo.value());
        int index = 0;
        
        for (AbstractInsnNode instruction : target.instructions.toArray()) {
            if (instruction.getOpcode() == opcode) {
                String nodeOpe = targetOpe.contains(" ") ? getFieldInsnNodeOperation(instruction) : getMethodInsnNodeOperation(instruction);
                if (targetOpe.isEmpty() || (nodeOpe != null && (nodeOpe.equals(targetOpe) || nodeOpe.contains(targetOpe)))) {
                    if (index == targetInfo.ordinal()) {
                        nodes.add(instruction);
                        return nodes;
                    } else {
                        index++;
                    }
                }
            }
        }
        return nodes;
    }

    private static void insert(String sourceName, String targetName, MethodNode source, MethodNode target, Inject info) {
        List<AbstractInsnNode> targetNodes = findTargetInsnNodes(target, info);
        if (targetNodes.isEmpty()) {
            if (Logger != null)
                Logger.error("No target found: {} in {}", info.target().value() + " " + info.target().target(), target.name);
            return;
        }

        if (info.target().value().equals("HEAD")) {
            AbstractInsnNode first = target.instructions.getFirst();
            if (first != null) {
                target.instructions.insertBefore(first, cloneInsnList(source.instructions, sourceName, targetName));
            } else {
                target.instructions.add(cloneInsnList(source.instructions, sourceName, targetName));
            }
            return;
        }

        Target.Shift shift = info.target().shift();

        for (AbstractInsnNode targetNode : targetNodes) {
            if (shift == Target.Shift.BEFORE) {
                target.instructions.insertBefore(targetNode, cloneInsnList(source.instructions, sourceName, targetName));
            } else if (shift == Target.Shift.AFTER) {
                target.instructions.insert(targetNode, cloneInsnList(source.instructions, sourceName, targetName));
            }
        }
    }

    private static InsnList cloneInsnList(InsnList source, String sourceName, String targetName) {
        InsnList clone = new InsnList();
        Map<LabelNode, LabelNode> labelMap = new HashMap<>();
        
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode) {
                labelMap.put((LabelNode) insn, new LabelNode());
            }
        }
        
        for (AbstractInsnNode insn = source.getFirst(); insn != null; insn = insn.getNext()) {
            int opcode = insn.getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                continue;
            }
            
            AbstractInsnNode cloned = insn.clone(labelMap);
            if (cloned instanceof FieldInsnNode) {
                FieldInsnNode fieldInsn = (FieldInsnNode) cloned;
                if (fieldInsn.owner.equals(sourceName)) fieldInsn.owner = targetName;
                
            } else if (cloned instanceof MethodInsnNode) {
                MethodInsnNode methodInsn = (MethodInsnNode) cloned;
                if (methodInsn.owner.equals(sourceName)) methodInsn.owner = targetName;
                
            } else if (cloned instanceof TypeInsnNode) {
                TypeInsnNode typeInsn = (TypeInsnNode) cloned;
                if (typeInsn.desc.equals(sourceName)) typeInsn.desc = targetName;
            }
            
            clone.add(cloned);
        }
        return clone;
    }
}