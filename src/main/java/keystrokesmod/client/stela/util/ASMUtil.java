package keystrokesmod.client.stela.util;

import keystrokesmod.client.stela.Stela;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import java.util.ArrayList;
import java.util.List;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ASMUtil {

    public static List<AnnotationNode> getAnnotations(MethodNode method) {
        List<AnnotationNode> list = new ArrayList<>();
        if (method == null) return list;
        if (method.visibleAnnotations != null) list.addAll(method.visibleAnnotations);
        if (method.invisibleAnnotations != null) list.addAll(method.invisibleAnnotations);
        return list;
    }

    public static List<AnnotationNode> getAnnotations(FieldNode field) {
        List<AnnotationNode> list = new ArrayList<>();
        if (field == null) return list;
        if (field.visibleAnnotations != null) list.addAll(field.visibleAnnotations);
        if (field.invisibleAnnotations != null) list.addAll(field.invisibleAnnotations);
        return list;
    }

    public static List<AnnotationNode> getAnnotations(ClassNode clazz) {
        List<AnnotationNode> list = new ArrayList<>();
        if (clazz == null) return list;
        if (clazz.visibleAnnotations != null) list.addAll(clazz.visibleAnnotations);
        if (clazz.invisibleAnnotations != null) list.addAll(clazz.invisibleAnnotations);
        return list;
    }

    public static String slash(String s) {
        return s == null ? "" : s.replace('.', '/');
    }

    public static ClassNode node(byte[] bytes) {
        if (bytes != null && bytes.length != 0) {
            ClassReader reader = new ClassReader(bytes);
            ClassNode node = new ClassNode();
            reader.accept(node, 0);
            return node;
        }
        return null;
    }

    public static String readClassName(byte[] bytes) {
        return new ClassReader(bytes).getClassName();
    }

    public static byte[] rewriteClass(ClassReader reader, ClassNode node) {
        ClassWriter writer = new ClassWriter(COMPUTE_MAXS | COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                try {
                    Class<?> class1 = Stela.classProvider != null ? Stela.classProvider.get(type1) : null;
                    Class<?> class2 = Stela.classProvider != null ? Stela.classProvider.get(type2) : null;
                    if (class1 != null && class2 != null) {
                        if (class1.isAssignableFrom(class2)) {
                            return type1;
                        } else if (class2.isAssignableFrom(class1)) {
                            return type2;
                        } else if (!class1.isInterface() && !class2.isInterface()) {
                            do {
                                class1 = class1.getSuperclass();
                            } while (!class1.isAssignableFrom(class2));
                            return class1.getName().replace('.', '/');
                        }
                    }
                } catch (Throwable ignored) {
                }
                return "java/lang/Object";
            }
        };
        node.accept(writer);
        return writer.toByteArray();
    }

    @SuppressWarnings("unchecked")
    public static <T> T getAnnotationValue(AnnotationNode node, String name) {
        if (node != null && node.values != null) {
            for (int i = 0; i < node.values.size(); i += 2) {
                if (node.values.get(i).equals(name)) {
                    Object obj = node.values.get(i + 1);
                    return (T) obj;
                }
            }
        }
        return null;
    }

    public static String[] split(String str, String splitter) {
        if (!str.contains(splitter))
            return new String[]{};
        ArrayList<String> result = new ArrayList<>();
        StringBuilder stringBuilder = new StringBuilder();
        StringBuilder passed = new StringBuilder();
        for (int i = 0; i < str.length() - (splitter.length() - 1); i++) {
            StringBuilder sb = new StringBuilder();
            for (int j = i; j < i + splitter.length(); j++)
                sb.append(str.charAt(j));
            if (sb.toString().equals(splitter)) {
                result.add(stringBuilder.toString());
                passed.append(stringBuilder);
                passed.append(splitter);
                stringBuilder = new StringBuilder();
                i += splitter.length();
            }
            if (i < str.length() - 1)
                stringBuilder.append(str.charAt(i));
        }
        String last = str.replace(passed.toString(), "");
        if (!last.isEmpty())
            result.add(last);
        return result.toArray(new String[0]);
    }
}
