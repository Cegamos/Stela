package keystrokesmod.client.stela.util;

import keystrokesmod.client.stela.Stela;

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

    public static void readMapping(String content, ArrayList<Map> dest) {
        content = content.replace("\r", "\n");
        dest.clear();
        for (String line : content.split("\n")) {
            line = line.replace("\n", "");
            if (line.length() <= 4) continue;
            String[] values = line.substring(4).split(" ");
            String[] obf, friendly;
            switch (line.substring(0, 2)) {
                case "CL":
                    dest.add(new Map(null, values[1], null, values[0], Type.Class));
                    break;
                case "FD":
                    if (values.length == 4) {
                        obf = ASMUtil.split(values[0], "/");
                        friendly = ASMUtil.split(values[2], "/");
                        dest.add(new Map(
                                values[2].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                values[3],
                                obf[obf.length - 1],
                                Mapper.Type.Field
                        ));
                    } else if (values.length == 2) {
                        obf = ASMUtil.split(values[0], "/");
                        friendly = ASMUtil.split(values[1], "/");
                        dest.add(new Map(
                                values[1].replace("/" + friendly[friendly.length - 1], ""),
                                friendly[friendly.length - 1],
                                null,
                                obf[obf.length - 1],
                                Mapper.Type.Field
                        ));
                    }
                    break;
                case "MD":
                    obf = ASMUtil.split(values[0], "/");
                    friendly = ASMUtil.split(values[2], "/");
                    dest.add(
                            new Map(
                                    values[2].replace("/" + friendly[friendly.length - 1], ""),
                                    friendly[friendly.length - 1],
                                    values[3],
                                    obf[obf.length - 1],
                                    Type.Method
                            )
                    );
            }
        }
    }

    public static void readMappings(String vanillaContent) {
        readMapping(vanillaContent, getVanilla());
    }

    private static final java.util.Map<String, String> cache = new HashMap<>();

    public static java.util.Map<String, String> getCache() {
        return cache;
    }

    public static String map(String owner, String name, String desc, Type type) {
        if (owner != null) owner = owner.replace('.', '/');
        String identifier = owner + "." + name + " " + desc;
        String value = cache.get(identifier);
        if (value != null) return value;
        String finalOwner = owner;
        Map map = mappings.stream().filter(m ->
                m.type == type &&
                        (type == Type.Class || finalOwner == null || m.owner.equals(finalOwner.replace('.', '/'))) &&
                        (m.name.equals(name.replace('.', '/'))) &&
                        (type == Type.Class || desc == null || m.desc.equals(desc))
        ).findFirst().orElse(new Map(owner, name, "null", name, type));
        String result = applyMode(map);
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
