package keystrokesmod.client.stela;

import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.operation.impl.*;
import net.minecraft.launchwrapper.IClassTransformer;
import net.minecraft.launchwrapper.Launch;
import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StelaTransformer implements IClassTransformer {
    private static final Map<String, List<Mixin>> mixinsByTarget = new HashMap<>();
    private static final List<Operation> operations = new ArrayList<>();
    private static boolean initialized = false;

    public static synchronized void init() {
        if (initialized) return;
        initialized = true;

        Stela.init(
                name -> Class.forName(name.replace('/', '.'), false, StelaTransformer.class.getClassLoader()),
                new Logger() {
                    @Override public void info(String msg, Object... args) { System.out.println("[Stela INFO] " + format(msg, args)); }
                    @Override public void warn(String msg, Object... args) { System.out.println("[Stela WARN] " + format(msg, args)); }
                    @Override public void error(String msg, Object... args) { System.err.println("[Stela ERROR] " + format(msg, args)); }
                    @Override public void exception(Throwable t) { t.printStackTrace(); }
                    private String format(String msg, Object... args) {
                        for (Object arg : args) msg = msg.replaceFirst("\\{\\}", String.valueOf(arg));
                        return msg;
                    }
                }
        );

        operations.clear();
        operations.add(new InjectOperation());
        operations.add(new OverwriteOperation());
        operations.add(new ModifyOperation());
        operations.add(new RedirectOperation());
        operations.add(new ModifyConstantOperation());
        operations.add(new ModifyArgOperation());
        operations.add(new AccessorOperation());
        operations.add(new ModifyReturnValueOperation());
        operations.add(new InjectIfOperation());
        operations.add(new BeforeFieldAccessOperation());
        operations.add(new AfterFieldAccessOperation());

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
                    reader.accept(sourceNode, 0);

                    Mixin mixin = new Mixin(sourceNode, provider);
                    if (mixin.getTargetName() != null && !mixin.getTargetName().isEmpty()) {
                        String targetClassName = mixin.getTargetName().replace('/', '.');
                        mixinsByTarget.computeIfAbsent(targetClassName, k -> new ArrayList<>()).add(mixin);
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
    }

    private static byte[] getClassBytes(String className) {
        String path = className.replace('.', '/') + ".class";
        ClassLoader[] loaders = new ClassLoader[] {
            Launch.classLoader,
            Thread.currentThread().getContextClassLoader(),
            StelaTransformer.class.getClassLoader(),
            ClassLoader.getSystemClassLoader()
        };

        for (ClassLoader cl : loaders) {
            if (cl == null) continue;
            try (InputStream is = cl.getResourceAsStream(path)) {
                if (is != null) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] buffer = new byte[4096];
                    int n;
                    while ((n = is.read(buffer)) != -1) {
                        baos.write(buffer, 0, n);
                    }
                    byte[] bytes = baos.toByteArray();
                    if (bytes.length > 0) return bytes;
                }
            } catch (Throwable ignored) {}
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

                byte[] result = keystrokesmod.client.stela.util.ASMUtil.rewriteClass(reader, targetNode);
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
