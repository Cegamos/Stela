package keystrokesmod.client.stela;

import keystrokesmod.client.stela.annotations.Mixin.Info;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.tree.ClassNode;

import static keystrokesmod.client.stela.Stela.Logger;

public class Mixin {
    private byte[] targetOldBytes = null;
    private final ClassNode source;
    private ClassNode target;
    private final String targetName;

    public Mixin(ClassNode source, ClassBytesProvider provider) throws Throwable {
        this.source = source;
        Info info = Info.getInfo(source);
        if (info == null) {
            targetName = "";
            if (Logger != null) {
                Logger.warn("Class {} does not have @Mixin annotation", source.name);
            }
            return;
        }
        String targetClassName = info.targetClassName;
        targetName = targetClassName.replace('.', '/');
        if (Logger != null) {
            Logger.info("Loading mixin {} target class {}", source.name, targetName);
        }
        try {
            targetOldBytes = provider.getClassBytes(targetClassName);
            if (targetOldBytes != null) {
                target = ASMUtil.node(targetOldBytes);
            }
        } catch (Throwable t) {
            if (Logger != null) {
                Logger.warn("Target class {} for mixin {} byte lookup failed: {}", targetClassName, source.name, t.getMessage());
            }
        }
        if (target == null) {
            if (Logger != null)
                Logger.error("Failed to load target class {} for mixin {}", targetClassName, source.name);
        } else {
            if (Logger != null)
                Logger.info("Loaded target class {} for mixin {}", targetClassName, source.name);
        }
    }

    public void setTarget(ClassNode targetNode, byte[] bytes) {
        this.target = targetNode;
        this.targetOldBytes = bytes;
    }

    public byte[] getTargetOldBytes() {
        return targetOldBytes;
    }

    public ClassNode getSource() {
        return source;
    }

    public ClassNode getTarget() {
        return target;
    }

    public String getTargetName() {
        return targetName;
    }
}
