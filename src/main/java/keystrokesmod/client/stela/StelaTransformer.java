package keystrokesmod.client.stela;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.operation.impl.*;
import keystrokesmod.client.stela.util.ASMUtil;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;

public class StelaTransformer implements IClassTransformer {
    
    private static final Map<String, List<Mixin>> mixinsByTarget = new ConcurrentHashMap<>();
    
    private static final Operation[] operations = new Operation[] {
        new ShadowOperation(),
        new UniqueOperation(),
        new InjectOperation(), new OverwriteOperation(), new ModifyOperation(),
        new RedirectOperation(), new ModifyConstantOperation(), new ModifyArgOperation(),
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

        String[] mixinClassNames = new String[] {
            "keystrokesmod.client.mixin.mixins.MixinEntityPlayer",
            "keystrokesmod.client.mixin.mixins.MixinEntityPlayerSP",
            "keystrokesmod.client.mixin.mixins.MixinFontRenderer",
            "keystrokesmod.client.mixin.mixins.MixinGuiChat",
            "keystrokesmod.client.mixin.mixins.MixinMinecraft",
            "keystrokesmod.client.mixin.mixins.MixinNetworkManager",
            "keystrokesmod.client.mixin.mixins.MixinEntityRenderer"
        };

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
                        Stela.Logger.info("Registered Stela mixin {} -> {}", className, targetClassName);
                    }
                } else {
                    Stela.Logger.warn("Could not find class bytes for mixin: {}", className);
                }
            } catch (Throwable t) {
                Stela.Logger.error("Failed to load mixin class {}", className);
                t.printStackTrace();
            }
        }
        
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
            Stela.Logger.info("Applying {} Stela mixin(s) to target {}", mixins.size(), transformedName);
            try {
                ClassReader reader = new ClassReader(basicClass);
                ClassNode targetNode = new ClassNode();
                reader.accept(targetNode, 0);

                for (Mixin mixin : mixins) {
                    Stela.Logger.info("Executing mixin operations from {} -> {}", mixin.getSource().name, transformedName);
                    mixin.setTarget(targetNode, basicClass);
                    
                    for (Operation operation : operations) {
                        try {
                            operation.dispose(mixin);
                        } catch (Throwable opErr) {
                            Stela.Logger.error("Operation {} failed on mixin {}", operation.getClass().getSimpleName(), mixin.getSource().name);
                            opErr.printStackTrace();
                        }
                    }
                }

                byte[] result = ASMUtil.rewriteClass(reader, targetNode);
                Stela.Logger.info("Successfully transformed {} (Original: {} bytes -> Result: {} bytes)", transformedName, basicClass.length, result.length);
                return result;
                
            } catch (Throwable t) {
                Stela.Logger.error("Critical error while transforming {}", transformedName);
                t.printStackTrace();
            }
        }

        return basicClass;
    }
}