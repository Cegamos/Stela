package keystrokesmod.client.stela.operation.impl;

import keystrokesmod.client.stela.Mixin;
import keystrokesmod.client.stela.operation.Operation;
import org.objectweb.asm.tree.ClassNode;

public class ModifyOperation implements Operation {
    @Override
    public void dispose(Mixin mixin) {
        ClassNode target = mixin.getTarget();
        if (target == null) return;
    }
}
