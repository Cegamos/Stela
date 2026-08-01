package keystrokesmod.client.stela.util;

import keystrokesmod.client.stela.Stela;

import java.io.BufferedReader;
import java.io.StringReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;

public class Mapper {
    public static class Map {
        private final String owner, name, desc, obf;
        private final Type type;

        public Map(String owner, String name, String desc, String obf, Type type) {
            this.owner = owner;
            this.name = name;
            this.desc = desc;
            this.obf = obf;
            this.type = type;
        }

        public String getObf() {
            return obf;
        }

        public String getDesc() {
            return desc;
        }

        public String getName() {
            return name;
        }

        public String getOwner() {
            return owner;
        }

        public Type getType() {
            return type;
        }
    }

    public enum Type {
        Class, Field, Method
    }

    public enum Mode {
        None, Vanilla;
    }

    private static ArrayList<Map> mappings = new ArrayList<>();
    private static final ArrayList<Map> vanilla = new ArrayList<>();
    public static Mode mode = Mode.None;

    public static ArrayList<Map> getMappings() {
        return mappings;
    }

    public static Mode getMode() {
        return mode;
    }

    public static ArrayList<Map> getVanilla() {
        return vanilla;
    }

    public static void readMappingsFromReader(BufferedReader reader) {
        vanilla.clear();
        cache.clear();
        String line;
        try {
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.length() <= 4) continue;
                try {
                    String typeStr = line.substring(0, 2);
                    String rest = line.substring(3).trim();
                    String[] values = rest.split("\\s+");
                    if (values.length < 2) continue;

                    switch (typeStr) {
                        case "CL":
                            if (values.length >= 2) {
                                vanilla.add(new Map(null, values[1], null, values[0], Type.Class));
                            }
                            break;
                        case "FD":
                            if (values.length >= 4) {
                                String[] obf = values[0].split("/");
                                String[] friendly = values[2].split("/");
                                String owner = values[2].contains("/") ? values[2].substring(0, values[2].lastIndexOf('/')) : "";
                                vanilla.add(new Map(owner, friendly[friendly.length - 1], values[3], obf[obf.length - 1], Type.Field));
                            } else if (values.length >= 2) {
                                String[] obf = values[0].split("/");
                                String[] friendly = values[1].split("/");
                                String owner = values[1].contains("/") ? values[1].substring(0, values[1].lastIndexOf('/')) : "";
                                vanilla.add(new Map(owner, friendly[friendly.length - 1], null, obf[obf.length - 1], Type.Field));
                            }
                            break;
                        case "MD":
                            if (values.length >= 4) {
                                String[] obf = values[0].split("/");
                                String[] friendly = values[2].split("/");
                                String owner = values[2].contains("/") ? values[2].substring(0, values[2].lastIndexOf('/')) : "";
                                vanilla.add(new Map(owner, friendly[friendly.length - 1], values[3], obf[obf.length - 1], Type.Method));
                            }
                            break;
                    }
                } catch (Throwable ignored) {
                }
            }
        } catch (Throwable ignored) {
        }
    }

    public static void readMappings(String vanillaContent) {
        if (vanillaContent == null || vanillaContent.isEmpty()) return;
        try (BufferedReader reader = new BufferedReader(new StringReader(vanillaContent))) {
            readMappingsFromReader(reader);
        } catch (Throwable ignored) {}
    }

    private static final java.util.Map<String, String> cache = new HashMap<>();

    public static java.util.Map<String, String> getCache() {
        return cache;
    }

    public static String map(String owner, String name, String desc, Type type) {
        if (name == null || name.isEmpty()) return "";
        if (owner != null) owner = owner.replace('.', '/');
        String identifier = (owner != null ? owner : "") + "." + name + " " + (desc != null ? desc : "");
        String value = cache.get(identifier);
        if (value != null) return value;

        String finalOwner = owner;
        Map map = mappings.stream().filter(m ->
                m.type == type &&
                        (type == Type.Class || finalOwner == null || m.owner == null || m.owner.isEmpty() || m.owner.equals(finalOwner)) &&
                        (m.name.equals(name)) &&
                        (type == Type.Class || desc == null || desc.isEmpty() || m.desc == null || m.desc.isEmpty() || m.desc.equals(desc))
        ).findFirst().orElse(null);

        String result = (map != null) ? applyMode(map) : name;
        cache.put(identifier, result);
        return result;
    }

    public static void setMode(Mode mode) {
        Mapper.mode = mode;
        switch (mode) {
            case Vanilla:
                mappings = vanilla;
                break;
            case None:
                break;
        }
    }

    public static String applyMode(Map map) {
        if (mode == Mode.Vanilla) {
            return map.obf;
        }
        return map.name;
    }

    public static String mapWithSuper(String owner, String name, String desc, Type type) {
        if (owner == null) return name;
        owner = owner.replace('.', '/');
        String identifier = owner + "." + name + " " + desc;
        String value = cache.get(identifier);
        if (value != null) return value;
        java.util.Map<String, Map> owners = new HashMap<>();
        mappings.stream().filter(m ->
                m.type == type && m.name.equals(name) && (desc == null || m.desc == null || desc.equals(m.desc))
        ).forEach(m -> owners.put(m.owner, m));
        String mappedOwner = map(null, owner, null, Type.Class);
        Class<?> theClass = null;
        try {
            if (Stela.classProvider != null) {
                theClass = Stela.classProvider.get(mappedOwner);
            }
        } catch (ClassNotFoundException ignored) {
        }
        while (theClass != null && theClass != Object.class) {
            Class<?> finalTheClass = theClass;
            java.util.Map.Entry<String, Map> entry = owners.entrySet().stream()
                    .filter(m -> map(null, m.getKey(), null, Type.Class).equals(finalTheClass.getName().replace('.', '/')))
                    .findFirst().orElse(null);
            if (entry != null) {
                cache.put(identifier, applyMode(entry.getValue()));
                return applyMode(entry.getValue());
            }
            theClass = theClass.getSuperclass();
        }
        return name;
    }

    public static String mapMethodWithSuper(String owner, String name, String desc) {
        return mapWithSuper(owner, name, desc, Type.Method);
    }

    public static String mapFieldWithSuper(String owner, String name, String desc) {
        return mapWithSuper(owner, name, desc, Type.Field);
    }

    public static String getObfClass(String name) {
        return map(null, name, null, Type.Class);
    }

    public static String getFriendlyClass(String obf) {
        String value = cache.get(obf);
        if (value != null) return value;
        Map map = mappings.stream().filter(m -> m.type == Type.Class && m.obf.equals(obf.replace('.', '/'))).findFirst().orElse(null);
        if (map != null) {
            cache.put(obf, map.name);
            return map.name;
        }
        return obf;
    }
}
