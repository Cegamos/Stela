package wtf.stela;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import wtf.stela.operation.Operation;
import wtf.stela.operation.impl.*;
import wtf.stela.util.ASMUtil;
import wtf.stela.util.Mapper;

public class StelaTransformer implements IClassTransformer {
    
    private static final Map<String, List<Mixin>> mixinsByTarget = new ConcurrentHashMap<>();
    
    private static final Operation[] operations = new Operation[] {
        new ShadowOperation(),
        new UniqueOperation(),
        new InjectOperation(), new OverwriteOperation(), new ModifyOperation(),
        new RedirectOperation(), new ModifyConstantOperation(), new ModifyArgOperation(),
        new ModifyVariableOperation(), new ModifyReceiverOperation(),
        new AccessorOperation(), new ModifyReturnValueOperation(), new InjectIfOperation(),
        new BeforeFieldAccessOperation(), new AfterFieldAccessOperation()
    };

    private static volatile boolean initialized = false;

    private static final ClassLoader[] classLoaders = new ClassLoader[] {
        Launch.classLoader,
        Thread.currentThread().getContextClassLoader(),
        StelaTransformer.class.getClassLoader(),
        ClassLoader.getSystemClassLoader()
    };

    public static synchronized void init() {
        if (initialized) return;

        Stela.init(
                name -> {
                    try {
                        return Class.forName(name.replace('/', '.'), false, StelaTransformer.class.getClassLoader());
                    } catch (ClassNotFoundException e) {
                        return null;
                    }
                },
                new Logger() {
                    @Override public void info(String msg, Object... args) { System.out.println("[Stela INFO] " + formatFast(msg, args)); }
                    @Override public void warn(String msg, Object... args) { System.out.println("[Stela WARN] " + formatFast(msg, args)); }
                    @Override public void error(String msg, Object... args) { System.err.println("[Stela ERROR] " + formatFast(msg, args)); }
                    @Override public void exception(Throwable t) { t.printStackTrace(); }
                    

                    private String formatFast(String msg, Object... args) {
                        if (args == null || args.length == 0 || msg == null) return msg;
                        StringBuilder sb = new StringBuilder(msg.length() + 32);
                        int argIndex = 0;
                        int lastMatch = 0;
                        int match;
                        
                        while ((match = msg.indexOf("{}", lastMatch)) != -1 && argIndex < args.length) {
                            sb.append(msg, lastMatch, match);
                            sb.append(args[argIndex++]);
                            lastMatch = match + 2;
                        }
                        sb.append(msg.substring(lastMatch));
                        return sb.toString();
                    }
                }
        );

        ClassBytesProvider provider = StelaTransformer::getClassBytes;

        boolean isDev = isDevEnvironment();
        try (InputStream is = getSrgStream()) {
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
                    Mapper.readMappingsFromReader(reader);
                    if (isDev) {
                        Mapper.setMode(Mapper.Mode.None);
                        Stela.Logger.info("Development environment detected (deobfuscated)! Mappings loaded but remapping disabled.");
                    } else {
                        Mapper.setMode(Mapper.Mode.Vanilla);
                        Stela.Logger.info("Production environment detected! Loaded stela.srg mappings ({} entries).", Mapper.getVanilla().size());
                    }
                }
            } else {
                Stela.Logger.warn("Could not find /stela.srg in resources!");
                if (isDev) {
                    Mapper.setMode(Mapper.Mode.None);
                }
            }
        } catch (Throwable t) {
            Stela.Logger.error("Failed to load /stela.srg mappings: {}", t.getMessage());
        }

        String[] mixinClassNames = new String[] {
            "wtf.mixin.mixins.MixinEntityPlayer",
            "wtf.mixin.mixins.MixinEntityPlayerSP",
            "wtf.mixin.mixins.MixinFontRenderer",
            "wtf.mixin.mixins.MixinGuiChat",
            "wtf.mixin.mixins.MixinMinecraft",
            "wtf.mixin.mixins.MixinNetworkManager",
            "wtf.mixin.mixins.MixinEntityRenderer",
            "wtf.mixin.mixins.MixinMovementInputFromOptions"
        };

        int registeredMixins = 0;
        for (String className : mixinClassNames) {
            try {
                byte[] bytes = getClassBytes(className);
                if (bytes != null) {
                    ClassReader reader = new ClassReader(bytes);
                    ClassNode sourceNode = new ClassNode();
                    reader.accept(sourceNode, ClassReader.SKIP_FRAMES);

                    Mixin mixin = new Mixin(sourceNode, provider);
                    if (mixin.getTargetName() != null && !mixin.getTargetName().isEmpty()) {
                        String targetClassName = mixin.getTargetName().replace('/', '.');
                        mixinsByTarget.computeIfAbsent(targetClassName, k -> new CopyOnWriteArrayList<>()).add(mixin);
                        registeredMixins++;
                    }
                } else {
                    Stela.Logger.warn("Could not find class bytes for mixin: {}", className);
                }
            } catch (Throwable t) {
                Stela.Logger.error("Failed to load mixin class {}", className);
                t.printStackTrace();
            }
        }
        
        Stela.Logger.info("Successfully registered {} mixins.", registeredMixins);
        initialized = true;
    }

    private static byte[] getClassBytes(String className) {
        String path = className.replace('.', '/') + ".class";

        for (ClassLoader cl : classLoaders) {
            if (cl == null) continue;
            
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream(8192);
                    byte[] buffer = new byte[8192]; 
                    int n;
                    while ((n = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, n);
                    }
                    return baos.toByteArray();
                }
            } catch (Throwable ignored) {
            }
        }

        try {
            return Launch.classLoader.getClassBytes(className);
        } catch (Throwable ignored) {}

        return null;
    }

    @Override
    public byte[] transform(String name, String transformedName, byte[] basicClass) {
        if (!initialized) {
            init();
        }

        if (basicClass == null || transformedName == null) {
            return basicClass;
        }

        List<Mixin> mixins = mixinsByTarget.get(transformedName);
        if (mixins != null && !mixins.isEmpty()) {
            try {
                ClassReader reader = new ClassReader(basicClass);
                ClassNode targetNode = new ClassNode();
                reader.accept(targetNode, 0);

                for (Mixin mixin : mixins) {
                    mixin.setTarget(targetNode, basicClass);
                    
                    for (Operation operation : operations) {
                        try {
                            operation.dispose(mixin);
                        } catch (Throwable opErr) {
                            Stela.Logger.error("Operation {} failed on mixin {} for target {}", operation.getClass().getSimpleName(), mixin.getSource().name, transformedName);
                            opErr.printStackTrace();
                        }
                    }
                }

                return ASMUtil.rewriteClass(reader, targetNode);
                
            } catch (Throwable t) {
                Stela.Logger.error("Critical error while transforming {}", transformedName);
                t.printStackTrace();
            }
        }

        return basicClass;
    }

    private static InputStream getSrgStream() {
        String[] paths = new String[] {"/stela.srg", "stela.srg", "/forge.srg", "forge.srg"};
        for (String path : paths) {
            InputStream is = StelaTransformer.class.getResourceAsStream(path);
            if (is != null) return is;
            if (StelaTransformer.class.getClassLoader() != null) {
                is = StelaTransformer.class.getClassLoader().getResourceAsStream(path);
                if (is != null) return is;
            }
            if (Thread.currentThread().getContextClassLoader() != null) {
                is = Thread.currentThread().getContextClassLoader().getResourceAsStream(path);
                if (is != null) return is;
            }
        }
        return null;
    }

    public static boolean isDevEnvironment() {
        try {
            Boolean deobf = (Boolean) Launch.blackboard.get("fml.deobfuscatedEnvironment");
            if (deobf != null && deobf) return true;
            byte[] bytes = getClassBytes("net.minecraft.client.Minecraft");
            if (bytes != null) {
                String content = new String(bytes, StandardCharsets.ISO_8859_1);
                if (content.contains("runTick") && !content.contains("func_71411_J")) {
                    return true;
                }
            }
        } catch (Throwable ignored) {}
        return false;
    }
}