package wtf.stela.util;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.ClassWriter;
import org.objectweb.asm.tree.AnnotationNode;
import org.objectweb.asm.tree.ClassNode;
import org.objectweb.asm.tree.FieldNode;
import org.objectweb.asm.tree.MethodNode;

import wtf.stela.Stela;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;
import java.util.regex.Pattern;

import static org.objectweb.asm.ClassWriter.COMPUTE_FRAMES;
import static org.objectweb.asm.ClassWriter.COMPUTE_MAXS;

public class ASMUtil {

    private static final ConcurrentMap<String, String> classCache = new ConcurrentHashMap<>();

    public static List<AnnotationNode> getAnnotations(MethodNode method) {
        if (method == null) return Collections.emptyList();
        if (method.visibleAnnotations == null && method.invisibleAnnotations == null) return Collections.emptyList();
        
        List<AnnotationNode> list = new ArrayList<>();
        if (method.visibleAnnotations != null) list.addAll(method.visibleAnnotations);
        if (method.invisibleAnnotations != null) list.addAll(method.invisibleAnnotations);
        return list;
    }

    public static List<AnnotationNode> getAnnotations(FieldNode field) {
        if (field == null) return Collections.emptyList();
        if (field.visibleAnnotations == null && field.invisibleAnnotations == null) return Collections.emptyList();
        
        List<AnnotationNode> list = new ArrayList<>();
        if (field.visibleAnnotations != null) list.addAll(field.visibleAnnotations);
        if (field.invisibleAnnotations != null) list.addAll(field.invisibleAnnotations);
        return list;
    }

    public static List<AnnotationNode> getAnnotations(ClassNode clazz) {
        if (clazz == null) return Collections.emptyList();
        if (clazz.visibleAnnotations == null && clazz.invisibleAnnotations == null) return Collections.emptyList();
        
        List<AnnotationNode> list = new ArrayList<>();
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
            reader.accept(node, ClassReader.SKIP_DEBUG);
            return node;
        }
        return null;
    }

    public static String readClassName(byte[] bytes) {
        return new ClassReader(bytes).getClassName();
    }

    public static byte[] rewriteClass(ClassReader reader, ClassNode node) {
        ClassWriter writer = new ClassWriter(reader, COMPUTE_MAXS | COMPUTE_FRAMES) {
            @Override
            protected String getCommonSuperClass(String type1, String type2) {
                if (type1 == null || type2 == null) return "java/lang/Object";
                if (type1.equals("java/lang/Object") || type2.equals("java/lang/Object")) return "java/lang/Object";
                if (type1.equals(type2)) return type1;

                String cacheKey = type1 + "|" + type2;
                String cached = classCache.get(cacheKey);
                if (cached != null) return cached;

                String commonSuper = resolveSuperClassSafe(type1, type2);
                
                classCache.put(cacheKey, commonSuper);
                classCache.put(type2 + "|" + type1, commonSuper);
                return commonSuper;
            }

            private String resolveSuperClassSafe(String type1, String type2) {
                try {
                    Class<?> class1 = Stela.classProvider != null ? Stela.classProvider.get(type1) : null;
                    Class<?> class2 = Stela.classProvider != null ? Stela.classProvider.get(type2) : null;
                    
                    if (class1 != null && class2 != null) {
                        if (class1.isAssignableFrom(class2)) return type1;
                        if (class2.isAssignableFrom(class1)) return type2;
                        
                        if (class1.isInterface() || class2.isInterface()) return "java/lang/Object";

                        do {
                            class1 = class1.getSuperclass();
                            if (class1 == null) return "java/lang/Object";
                        } while (!class1.isAssignableFrom(class2));
                        
                        return class1.getName().replace('.', '/');
                    }
                } catch (Throwable ignored) {}
                
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
                    return (T) node.values.get(i + 1);
                }
            }
        }
        return null;
    }

    public static String[] split(String str, String splitter) {
        if (str == null || str.isEmpty()) return new String[0];
        if (splitter == null || splitter.isEmpty()) return new String[]{str};
        
        return str.split(Pattern.quote(splitter));
    }
}