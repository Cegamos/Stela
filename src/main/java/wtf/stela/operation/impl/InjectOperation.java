package wtf.stela.operation.impl;

import static wtf.stela.Stela.Logger;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.objectweb.asm.Opcodes;
import org.objectweb.asm.Type;
import org.objectweb.asm.tree.AbstractInsnNode;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldInsnNode;
import org.objectweb.asm.tree.IincInsnNode;
import org.objectweb.asm.tree.InsnList;
import org.objectweb.asm.tree.InsnNode;
import org.objectweb.asm.tree.JumpInsnNode;
import org.objectweb.asm.tree.LabelNode;
import org.objectweb.asm.tree.LocalVariableNode;
import org.objectweb.asm.tree.MethodInsnNode;
import org.objectweb.asm.tree.MethodNode;
import org.objectweb.asm.tree.TryCatchBlockNode;
import org.objectweb.asm.tree.TypeInsnNode;
import org.objectweb.asm.tree.VarInsnNode;

import wtf.stela.Mixin;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Local;
import wtf.stela.annotations.Slice;
import wtf.stela.annotations.Target;
import wtf.stela.operation.Operation;
import wtf.stela.util.ASMUtil;
import wtf.stela.util.DescParser;
import wtf.stela.util.Mapper;

public class InjectOperation implements Operation {

    private static final String CALLBACK_INFO = "Lwtf/stela/CallbackInfo;";

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

    private static ArrayList<String[]> getLocalParameters(MethodNode source, MethodNode target) {
        ArrayList<String[]> parameters = new ArrayList<>();
        if (source.visibleParameterAnnotations == null) return parameters;

        List<AnnotationNode>[] paramAnnotations = source.visibleParameterAnnotations;
        boolean sourceStatic = (source.access & Opcodes.ACC_STATIC) != 0;
        boolean targetStatic = (target.access & Opcodes.ACC_STATIC) != 0;
        int sourceBase = sourceStatic ? 0 : 1;
        int targetBase = targetStatic ? 0 : 1;
        int sourceOffset = 0;
        int targetOffset = 0;
        Type[] targetArgTypes = Type.getArgumentTypes(target.desc);

        for (int i = 0; i < paramAnnotations.length; i++) {
            List<AnnotationNode> annotations = paramAnnotations[i];
            if (annotations == null) {
                if (i < targetArgTypes.length) targetOffset += targetArgTypes[i].getSize();
                continue;
            }

            AnnotationNode localAnno = null;
            boolean coerced = false;
            for (AnnotationNode annotationNode : annotations) {
                if (annotationNode == null) continue;
                if (annotationNode.desc.contains(ASMUtil.slash(Local.class.getName()))) {
                    localAnno = annotationNode;
                } else if (annotationNode.desc.equals("Lwtf/stela/annotations/Coerce;") ||
                        annotationNode.desc.equals("Lorg/spongepowered/asm/mixin/injection/Coerce;")) {
                    coerced = true;
                }
            }

            if (localAnno != null) {
                String sourceParam = ASMUtil.getAnnotationValue(localAnno, "source");
                String targetVar = ASMUtil.getAnnotationValue(localAnno, "target");
                Integer indexVal = ASMUtil.getAnnotationValue(localAnno, "index");
                int index = indexVal != null ? indexVal : -1;
                parameters.add(new String[]{sourceParam != null ? sourceParam : "", index != -1 ? String.valueOf(index) : (targetVar != null ? targetVar : "")});
            } else if (coerced) {
                int sourceSlot = sourceBase + sourceOffset;
                int targetSlot = targetBase + targetOffset;
                parameters.add(new String[]{String.valueOf(sourceSlot), String.valueOf(targetSlot)});
            }

            if (i < targetArgTypes.length) targetOffset += targetArgTypes[i].getSize();
            sourceOffset += 1;
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
        ArrayList<String[]> sourceParameters = getLocalParameters(source, target);

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
        if (source == null || target == null) return;

        List<MethodNode> injections = source.methods.stream()
                .filter(m -> getInjectAnnotation(m) != null)
                .collect(Collectors.toList());

        for (MethodNode injection : injections) {
            Inject info = getInjectAnnotation(injection);
            if (info == null) continue;

            MethodNode targetMethod = findTargetMethod(target.methods, mixin.getTargetName(), info.method(), info.desc());
            if (targetMethod == null) {
                if (Logger != null)
                    Logger.error("No method found: {} in {}", Arrays.toString(info.method()) + info.desc(), target.name);
                continue;
            }

            processLocalValues(injection, targetMethod);

            int callbackSlot = -1;
            if (info.cancellable()) {
                callbackSlot = prepareCancellable(injection, targetMethod);
            }

            try {
                insert(source.name, target.name, injection, targetMethod, info, callbackSlot);
            } catch (Throwable e) {
                if (Logger != null) Logger.exception(e);
            }
        }
    }

    private static boolean hasCallbackInfoParam(MethodNode source) {
        Type[] args = Type.getArgumentTypes(source.desc);
        return args.length > 0 && args[0].getDescriptor().equals(CALLBACK_INFO);
    }

    private static int prepareCancellable(MethodNode source, MethodNode target) {
        if (!hasCallbackInfoParam(source)) {
            if (Logger != null)
                Logger.warn("cancellable=true but handler {} does not take CallbackInfo as first argument", source.name);
            return -1;
        }

        int ciLocal = (source.access & Opcodes.ACC_STATIC) != 0 ? 0 : 1;
        int ciSlot = target.maxLocals;
        target.maxLocals = ciSlot + 1;

        for (int i = 0; i < source.instructions.size(); i++) {
            AbstractInsnNode instruction = source.instructions.get(i);
            if (instruction instanceof VarInsnNode && isLoadOpe(instruction.getOpcode()) && ((VarInsnNode) instruction).var == ciLocal) {
                ((VarInsnNode) instruction).var = ciSlot;
            }
        }
        return ciSlot;
    }

    public static List<String> parseMethodNames(Object methodNameObj) {
        List<String> result = new ArrayList<>();
        if (methodNameObj instanceof List) {
            for (Object item : (List<?>) methodNameObj) {
                if (item != null) {
                    for (String part : item.toString().split("[,|\\s]+")) {
                        if (!part.trim().isEmpty()) result.add(part.trim());
                    }
                }
            }
        } else if (methodNameObj instanceof String[]) {
            for (String s : (String[]) methodNameObj) {
                if (s != null) {
                    for (String part : s.split("[,|\\s]+")) {
                        if (!part.trim().isEmpty()) result.add(part.trim());
                    }
                }
            }
        } else if (methodNameObj != null) {
            for (String part : methodNameObj.toString().split("[,|\\s]+")) {
                if (!part.trim().isEmpty()) result.add(part.trim());
            }
        }
        return result;
    }

    public static Inject getInjectAnnotation(MethodNode method) {
        if (method == null) return null;
        for (AnnotationNode annotation : ASMUtil.getAnnotations(method)) {
            if (annotation.desc.equals("Lwtf/stela/annotations/Inject;") ||
                annotation.desc.equals("Lorg/spongepowered/asm/mixin/injection/Inject;")) {
                Object methodNameObj = ASMUtil.getAnnotationValue(annotation, "method");
                List<String> nameList = parseMethodNames(methodNameObj);
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

                Boolean cancellable = ASMUtil.getAnnotationValue(annotation, "cancellable");
                Slice slice = readSlice(ASMUtil.getAnnotationValue(annotation, "slice"));

                final String fTargetValue = targetValue;
                final String fTargetTarget = targetTarget;
                final Target.Shift fTargetShift = targetShift;
                final int fTargetOrdinal = targetOrdinal;
                final boolean fCancellable = cancellable != null && cancellable;
                final Slice fSlice = slice;

                Target targetAnno = new Target() {
                    @Override public Class<? extends Annotation> annotationType() { return Target.class; }
                    @Override public String value() { return fTargetValue; }
                    @Override public String target() { return fTargetTarget; }
                    @Override public Shift shift() { return fTargetShift; }
                    @Override public int ordinal() { return fTargetOrdinal; }
                };

                return new Inject() {
                    @Override public Class<? extends Annotation> annotationType() { return Inject.class; }
                    @Override public String[] method() { return methodArray; }
                    @Override public String desc() { return desc != null ? desc : ""; }
                    @Override public Target target() { return targetAnno; }
                    @Override public Slice slice() { return fSlice; }
                    @Override public boolean cancellable() { return fCancellable; }
                    @Override public boolean remap() { return true; }
                };
            }
        }
        return null;
    }

    private static Slice readSlice(Object sliceObj) {
        if (sliceObj == null) return null;
        AnnotationNode sliceNode = null;
        if (sliceObj instanceof AnnotationNode) {
            sliceNode = (AnnotationNode) sliceObj;
        } else if (sliceObj instanceof List && !((List<?>) sliceObj).isEmpty()) {
            Object first = ((List<?>) sliceObj).get(0);
            if (first instanceof AnnotationNode) sliceNode = (AnnotationNode) first;
        }
        if (sliceNode == null) return null;

        Target from = null;
        Target to = null;
        if (sliceNode.values != null) {
            for (int i = 0; i < sliceNode.values.size(); i += 2) {
                String key = (String) sliceNode.values.get(i);
                Object val = sliceNode.values.get(i + 1);
                if (key.equals("from") && val instanceof AnnotationNode) {
                    from = readTarget((AnnotationNode) val);
                } else if (key.equals("to") && val instanceof AnnotationNode) {
                    to = readTarget((AnnotationNode) val);
                }
            }
        }
        if (from == null && to == null) return null;
        final Target fFrom = from;
        final Target fTo = to;
        return new Slice() {
            @Override public Class<? extends Annotation> annotationType() { return Slice.class; }
            @Override public String id() { return ""; }
            @Override public Target from() { return fFrom != null ? fFrom : defaultHead(); }
            @Override public Target to() { return fTo != null ? fTo : defaultTail(); }
        };
    }

    private static Target defaultHead() {
        return new Target() {
            @Override public Class<? extends Annotation> annotationType() { return Target.class; }
            @Override public String value() { return "HEAD"; }
            @Override public String target() { return ""; }
            @Override public Shift shift() { return Shift.BEFORE; }
            @Override public int ordinal() { return 0; }
        };
    }

    private static Target defaultTail() {
        return new Target() {
            @Override public Class<? extends Annotation> annotationType() { return Target.class; }
            @Override public String value() { return "TAIL"; }
            @Override public String target() { return ""; }
            @Override public Shift shift() { return Shift.BEFORE; }
            @Override public int ordinal() { return 0; }
        };
    }

    private static Target readTarget(AnnotationNode node) {
        String value = "HEAD";
        String target = "";
        Target.Shift shift = Target.Shift.BEFORE;
        int ordinal = 0;
        if (node != null) {
            String tv = ASMUtil.getAnnotationValue(node, "value");
            if (tv != null) value = tv;
            String tt = ASMUtil.getAnnotationValue(node, "target");
            if (tt != null) target = tt;
            String[] ts = ASMUtil.getAnnotationValue(node, "shift");
            if (ts != null && ts.length > 1) {
                try { shift = Target.Shift.valueOf(ts[1]); } catch (Exception ignored) {}
            }
            Integer ord = ASMUtil.getAnnotationValue(node, "ordinal");
            if (ord != null) ordinal = ord;
        }
        final String fValue = value;
        final String fTarget = target;
        final Target.Shift fShift = shift;
        final int fOrdinal = ordinal;
        return new Target() {
            @Override public Class<? extends Annotation> annotationType() { return Target.class; }
            @Override public String value() { return fValue; }
            @Override public String target() { return fTarget; }
            @Override public Shift shift() { return fShift; }
            @Override public int ordinal() { return fOrdinal; }
        };
    }

    public static MethodNode findTargetMethod(List<MethodNode> methods, String targetOwner, Object methodNameObj, String desc) {
        List<String> possibleNames = parseMethodNames(methodNameObj);
        List<String> allNames = new ArrayList<>(possibleNames);

        for (String name : possibleNames) {
            String directMap = Mapper.map(targetOwner, name, desc, Mapper.Type.Method);
            if (directMap != null && !directMap.isEmpty() && !allNames.contains(directMap)) {
                allNames.add(directMap);
            }
            String globalMap = Mapper.map(null, name, desc, Mapper.Type.Method);
            if (globalMap != null && !globalMap.isEmpty() && !allNames.contains(globalMap)) {
                allNames.add(globalMap);
            }
        }

        List<String> descs = new ArrayList<>();
        if (desc != null && !desc.isEmpty()) {
            descs.add(desc);
            String mappedDesc = DescParser.mapDesc(desc);
            if (!mappedDesc.equals(desc)) descs.add(mappedDesc);
        }

        for (String name : allNames) {
            for (MethodNode method : methods) {
                if (method.name.equals(name) && (descs.isEmpty() || descs.contains(method.desc))) {
                    return method;
                }
            }
        }
        for (String name : allNames) {
            for (MethodNode method : methods) {
                if (method.name.equals(name)) {
                    return method;
                }
            }
        }
        return null;
    }

    public static MethodNode findTargetMethod(List<MethodNode> methods, String targetOwner, String methodName, String desc) {
        return findTargetMethod(methods, targetOwner, (Object) methodName, desc);
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
            String mappedMethod = Mapper.map(null, ope, null, Mapper.Type.Method);
            if (mappedMethod != null && !mappedMethod.isEmpty() && !mappedMethod.equals(ope)) {
                return mappedMethod;
            }
            String mappedField = Mapper.map(null, ope, null, Mapper.Type.Field);
            if (mappedField != null && !mappedField.isEmpty() && !mappedField.equals(ope)) {
                return mappedField;
            }
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

    public static List<AbstractInsnNode> findTargetInsns(MethodNode target, Inject info) {
        Slice slice = info.slice();
        boolean restricted = slice != null &&
                (!slice.from().value().equals("HEAD") || !slice.from().target().isEmpty() || slice.from().ordinal() != 0 ||
                 !slice.to().value().equals("TAIL") || !slice.to().target().isEmpty() || slice.to().ordinal() != 0);

        if (!restricted) {
            return findTargetInsnsPlain(target, info.target());
        }

        AbstractInsnNode from = findSliceBoundary(target, slice.from());
        AbstractInsnNode to = findSliceBoundary(target, slice.to());
        if (from == null || to == null) {
            return new ArrayList<>();
        }

        List<AbstractInsnNode> nodes = new ArrayList<>();
        int index = 0;
        for (AbstractInsnNode instruction = from; instruction != null; instruction = instruction.getNext()) {
            if (matchesTarget(target, info.target(), instruction)) {
                if (index == info.target().ordinal()) {
                    nodes.add(instruction);
                    return nodes;
                }
                index++;
            }
            if (instruction == to) break;
        }
        return nodes;
    }

    private static boolean matchesTarget(MethodNode target, Target targetInfo, AbstractInsnNode instruction) {
        if (targetInfo.value().equals("HEAD")) {
            return instruction.getOpcode() >= 0;
        }
        if (targetInfo.value().equals("TAIL")) {
            int opcode = instruction.getOpcode();
            return opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN;
        }
        String targetOpe = targetInfo.target().isEmpty() ? "" : mapOperation(targetInfo.target());
        int opcode = getOperationCode(targetInfo.value());
        if (instruction.getOpcode() != opcode) return false;
        String nodeOpe = targetOpe.contains(" ") ? getFieldInsnNodeOperation(instruction) : getMethodInsnNodeOperation(instruction);
        return targetOpe.isEmpty() || (nodeOpe != null && (nodeOpe.equals(targetOpe) || nodeOpe.contains(targetOpe)));
    }

    private static AbstractInsnNode findSliceBoundary(MethodNode target, Target boundary) {
        if (boundary.value().equals("HEAD")) {
            for (AbstractInsnNode insn : target.instructions.toArray()) {
                if (insn.getOpcode() >= 0) return insn;
            }
            return target.instructions.getFirst();
        }
        if (boundary.value().equals("TAIL")) {
            return target.instructions.getLast();
        }
        List<AbstractInsnNode> matches = findTargetInsnsPlain(target, boundary);
        return matches.isEmpty() ? null : matches.get(0);
    }

    public static List<AbstractInsnNode> findTargetInsns(MethodNode target, Target targetInfo) {
        return findTargetInsnsPlain(target, targetInfo);
    }

    private static List<AbstractInsnNode> findTargetInsnsPlain(MethodNode target, Target targetInfo) {
        List<AbstractInsnNode> nodes = new ArrayList<>();

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

    private static void insert(String sourceName, String targetName, MethodNode source, MethodNode target, Inject info, int callbackSlot) {
        List<AbstractInsnNode> targetNodes = findTargetInsns(target, info);
        if (targetNodes.isEmpty()) {
            if (Logger != null)
                Logger.error("No target found: {} in {}", info.target().value() + " " + info.target().target(), target.name);
            return;
        }

        if (info.target().value().equals("HEAD")) {
            AbstractInsnNode first = target.instructions.getFirst();
            InsnList block = buildInjectionBlock(source, target, sourceName, targetName, info, callbackSlot);
            if (first != null) {
                target.instructions.insertBefore(first, block);
            } else {
                target.instructions.add(block);
            }
            return;
        }

        Target.Shift shift = info.target().shift();

        for (AbstractInsnNode targetNode : targetNodes) {
            InsnList block = buildInjectionBlock(source, target, sourceName, targetName, info, callbackSlot);
            if (shift == Target.Shift.BEFORE) {
                target.instructions.insertBefore(targetNode, block);
            } else if (shift == Target.Shift.AFTER) {
                target.instructions.insert(targetNode, block);
            }
        }
    }

    private static InsnList buildInjectionBlock(MethodNode source, MethodNode target, String sourceName, String targetName, Inject info, int callbackSlot) {
        InsnList block = new InsnList();
        LabelNode continueLabel = new LabelNode();

        if (callbackSlot >= 0) {
            block.add(new TypeInsnNode(Opcodes.NEW, "wtf/stela/CallbackInfo"));
            block.add(new InsnNode(Opcodes.DUP));
            block.add(new MethodInsnNode(Opcodes.INVOKESPECIAL, "wtf/stela/CallbackInfo", "<init>", "()V", false));
            block.add(new VarInsnNode(Opcodes.ASTORE, callbackSlot));
        }

        cloneInsnList(source, target, sourceName, targetName, block);

        if (callbackSlot >= 0) {
            block.add(new VarInsnNode(Opcodes.ALOAD, callbackSlot));
            block.add(new MethodInsnNode(Opcodes.INVOKEVIRTUAL, "wtf/stela/CallbackInfo", "isCancelled", "()Z", false));
            block.add(new JumpInsnNode(Opcodes.IFEQ, continueLabel));
            block.add(createCancellationReturn(target));
        }

        block.add(continueLabel);
        return block;
    }

    private static InsnList createCancellationReturn(MethodNode target) {
        InsnList list = new InsnList();
        Type returnType = Type.getReturnType(target.desc);
        if (returnType.getSort() != Type.VOID) {
            switch (returnType.getSort()) {
                case Type.BOOLEAN:
                case Type.CHAR:
                case Type.BYTE:
                case Type.SHORT:
                case Type.INT:
                    list.add(new InsnNode(Opcodes.ICONST_0));
                    break;
                case Type.LONG:
                    list.add(new InsnNode(Opcodes.LCONST_0));
                    break;
                case Type.FLOAT:
                    list.add(new InsnNode(Opcodes.FCONST_0));
                    break;
                case Type.DOUBLE:
                    list.add(new InsnNode(Opcodes.DCONST_0));
                    break;
                default:
                    list.add(new InsnNode(Opcodes.ACONST_NULL));
                    break;
            }
            list.add(new InsnNode(returnType.getOpcode(Opcodes.IRETURN)));
        } else {
            list.add(new InsnNode(Opcodes.RETURN));
        }
        return list;
    }

    private static void cloneInsnList(MethodNode source, MethodNode target, String sourceName, String targetName, InsnList into) {
        Map<LabelNode, LabelNode> labelMap = new HashMap<>();

        for (AbstractInsnNode insn = source.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            if (insn instanceof LabelNode) {
                labelMap.put((LabelNode) insn, new LabelNode());
            }
        }

        for (AbstractInsnNode insn = source.instructions.getFirst(); insn != null; insn = insn.getNext()) {
            int opcode = insn.getOpcode();
            if (opcode >= Opcodes.IRETURN && opcode <= Opcodes.RETURN) {
                into.add(new InsnNode(returnPopOpcode(opcode)));
                continue;
            }
            AbstractInsnNode cloned = insn.clone(labelMap);
            remapInstruction(cloned, sourceName, targetName);
            into.add(cloned);
        }

        if (source.tryCatchBlocks != null) {
            for (TryCatchBlockNode tcb : source.tryCatchBlocks) {
                LabelNode start = labelMap.get(tcb.start);
                LabelNode end = labelMap.get(tcb.end);
                LabelNode handler = labelMap.get(tcb.handler);
                if (start != null && end != null && handler != null) {
                    target.tryCatchBlocks.add(new TryCatchBlockNode(start, end, handler, tcb.type));
                }
            }
        }
    }

    private static int returnPopOpcode(int returnOpcode) {
        switch (returnOpcode) {
            case Opcodes.LRETURN:
            case Opcodes.DRETURN:
                return Opcodes.POP2;
            case Opcodes.IRETURN:
            case Opcodes.FRETURN:
            case Opcodes.ARETURN:
                return Opcodes.POP;
            default:
                return Opcodes.NOP;
        }
    }

    public static void remapInstruction(AbstractInsnNode insn, String sourceName, String targetName) {
        if (insn instanceof FieldInsnNode) {
            FieldInsnNode fieldInsn = (FieldInsnNode) insn;
            if (fieldInsn.owner.equals(sourceName)) {
                fieldInsn.owner = targetName;
                String mappedField = Mapper.map(targetName, fieldInsn.name, fieldInsn.desc, Mapper.Type.Field);
                if (mappedField != null && !mappedField.isEmpty()) {
                    fieldInsn.name = mappedField;
                }
                String mappedDesc = DescParser.mapDesc(fieldInsn.desc);
                if (mappedDesc != null && !mappedDesc.isEmpty()) {
                    fieldInsn.desc = mappedDesc;
                }
            } else {
                String mappedField = Mapper.map(fieldInsn.owner, fieldInsn.name, fieldInsn.desc, Mapper.Type.Field);
                if (mappedField != null && !mappedField.isEmpty()) {
                    fieldInsn.name = mappedField;
                }
                String mappedOwner = Mapper.map(null, fieldInsn.owner, null, Mapper.Type.Class);
                if (mappedOwner != null && !mappedOwner.isEmpty()) {
                    fieldInsn.owner = mappedOwner;
                }
                String mappedDesc = DescParser.mapDesc(fieldInsn.desc);
                if (mappedDesc != null && !mappedDesc.isEmpty()) {
                    fieldInsn.desc = mappedDesc;
                }
            }
        } else if (insn instanceof MethodInsnNode) {
            MethodInsnNode methodInsn = (MethodInsnNode) insn;
            if (methodInsn.owner.equals(sourceName)) {
                methodInsn.owner = targetName;
                String mappedMethod = Mapper.map(targetName, methodInsn.name, methodInsn.desc, Mapper.Type.Method);
                if (mappedMethod != null && !mappedMethod.isEmpty()) {
                    methodInsn.name = mappedMethod;
                }
                String mappedDesc = DescParser.mapDesc(methodInsn.desc);
                if (mappedDesc != null && !mappedDesc.isEmpty()) {
                    methodInsn.desc = mappedDesc;
                }
            } else {
                String mappedMethod = Mapper.map(methodInsn.owner, methodInsn.name, methodInsn.desc, Mapper.Type.Method);
                if (mappedMethod != null && !mappedMethod.isEmpty()) {
                    methodInsn.name = mappedMethod;
                }
                String mappedOwner = Mapper.map(null, methodInsn.owner, null, Mapper.Type.Class);
                if (mappedOwner != null && !mappedOwner.isEmpty()) {
                    methodInsn.owner = mappedOwner;
                }
                String mappedDesc = DescParser.mapDesc(methodInsn.desc);
                if (mappedDesc != null && !mappedDesc.isEmpty()) {
                    methodInsn.desc = mappedDesc;
                }
            }
        } else if (insn instanceof TypeInsnNode) {
            TypeInsnNode typeInsn = (TypeInsnNode) insn;
            if (typeInsn.desc.equals(sourceName)) {
                typeInsn.desc = targetName;
            } else {
                String mappedType = Mapper.map(null, typeInsn.desc, null, Mapper.Type.Class);
                if (mappedType != null && !mappedType.isEmpty()) {
                    typeInsn.desc = mappedType;
                }
            }
        }
    }
}
