package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.modules.movement.Fly;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;

@ModuleInfo(name = "FallSpeed", category = Category.Player)
public class FallSpeed extends Mod {
    private final DescriptionValue dc = new DescriptionValue("Vanilla max: 3.92", this);
    private final NumberValue a = new NumberValue("Motion", this, 5.0, 0.0, 8.0, 0.1);
    private final BooleanValue b = new BooleanValue("Disable XZ motion", this, true);

    @Override
    public void update() {
        if (mc.thePlayer.fallDistance >= 2.5) {
            final Mod fly = Kevin.moduleManager.getModuleByClazz(Fly.class);
            final Mod noFall = Kevin.moduleManager.getModuleByClazz(NoFall.class);
            if ((fly != null && fly.isEnabled()) || (noFall != null && noFall.isEnabled())) {
                return;
            }
            if (mc.thePlayer.capabilities.isCreativeMode || mc.thePlayer.capabilities.isFlying) {
                return;
            }
            if (mc.thePlayer.isOnLadder() || mc.thePlayer.isInWater() || mc.thePlayer.isInLava()) {
                return;
            }
            mc.thePlayer.motionY = -a.getValue();
            if (b.getValue()) {
                mc.thePlayer.motionZ = 0.0;
                mc.thePlayer.motionX = 0.0;
            }
        }
    }
}
