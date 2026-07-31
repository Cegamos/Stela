//Deobfuscated with https://github.com/SimplyProgrammer/Minecraft-Deobfuscator3000 using mappings "C:\Users\Marii\Desktop\ma"!

//Decompiled by Procyon!

package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import net.minecraft.entity.Entity;

@ModuleInfo(name = "KeepSprint", category = Category.Movement)
public class KeepSprint extends Mod {
    private final DescriptionValue a = new DescriptionValue("Default is 40% motion reduction.", this);
    private final NumberValue b = new NumberValue("Slow %", this, 40.0, 0.0, 100.0, 1.0);
    private final BooleanValue c = new BooleanValue("Only reduce reach hits", this, false);
    
    @Override
    public void onAttackTargetEntityWithCurrentItem(final Entity en) {
        if (this != null && this.isEnabled()) {
            if (c.isToggled() && !mc.thePlayer.capabilities.isCreativeMode) {
                final double dist = mc.objectMouseOver.hitVec.distanceTo(mc.getRenderViewEntity().getPositionEyes(1.0f));
                double val;
                if (dist > 3.0) {
                    val = (100.0 - (float)b.getInput()) / 100.0;
                }
                else {
                    val = 0.6;
                }
                mc.thePlayer.motionX *= val;
                mc.thePlayer.motionZ *= val;
            }
            else {
                final double dist = (100.0 - (float)b.getInput()) / 100.0;
                mc.thePlayer.motionX *= dist;
                mc.thePlayer.motionZ *= dist;
            }
        } else {
        	mc.thePlayer.motionX *= 0.6;
            mc.thePlayer.motionZ *= 0.6;
        }
    }
}
