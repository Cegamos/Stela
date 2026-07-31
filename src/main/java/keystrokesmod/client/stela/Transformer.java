package keystrokesmod.client.stela;

import keystrokesmod.client.stela.operation.Operation;
import keystrokesmod.client.stela.operation.impl.*;
import keystrokesmod.client.stela.util.ASMUtil;

import org.objectweb.asm.ClassReader;
import org.objectweb.asm.tree.ClassNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import static keystrokesmod.client.stela.Stela.Logger;

public class Transformer {
    private final ClassBytesProvider provider;
    private final ArrayList<Mixin> mixins;
    private final ArrayList<Operation> operations;
    private final Map<String, byte[]> oldBytes = new HashMap<>();

    public Transformer(ClassBytesProvider classBytesProvider) {
        this.provider = classBytesProvider;
        this.mixins = new ArrayList<>();
        this.operations = new ArrayList<>();
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
    }

    public void addMixin(ClassNode node) throws Throwable {
        mixins.add(new Mixin(node, provider));
    }

    public void addMixin(byte[] bytes) throws Throwable {
        addMixin(ASMUtil.node(bytes));
    }

    public Map<String, byte[]> transform() {
        Map<String, byte[]> classMap = new HashMap<>();
        oldBytes.clear();
        for (Mixin mixin : mixins) {
            if (mixin.getTarget() == null) {
                if (Logger != null)
                    Logger.warn("Mixin {} has no target class, skipping.", mixin.getSource().name);
                continue;
            }
            String name = mixin.getTarget().name.replace('/', '.');
            oldBytes.put(name, mixin.getTargetOldBytes());
            for (Operation operation : operations)
                operation.dispose(mixin);
            try {
                byte[] old = mixin.getTargetOldBytes();
                ClassReader reader = (old != null && old.length > 0) ? new ClassReader(old) : null;
                byte[] class_bytes = ASMUtil.rewriteClass(reader, mixin.getTarget());
                classMap.put(name, class_bytes);
            } catch (Throwable e) {
                if (Logger != null) {
                    Logger.error("Failed to transform class " + name, e);
                    Logger.exception(e);
                }
            }
        }
        return classMap;
    }

    public ClassBytesProvider getProvider() {
        return provider;
    }

    public Map<String, byte[]> getOldBytes() {
        return oldBytes;
    }

    public ArrayList<Operation> getOperations() {
        return operations;
    }

    public ArrayList<Mixin> getMixins() {
        return mixins;
    }
}
