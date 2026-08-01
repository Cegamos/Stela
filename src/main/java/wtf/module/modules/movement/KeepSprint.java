package wtf.module.modules.movement;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.AttackEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.DescriptionValue;
import wtf.module.value.impl.NumberValue;

@ModuleInfo(name = "KeepSprint", category = Category.Movement)
public class KeepSprint extends Mod {
    private final DescriptionValue desc = new DescriptionValue("Default is 40% motion reduction.", this);
    private final NumberValue slow = new NumberValue("Slow %", this, 40.0, 0.0, 100.0, 1.0);
    private final BooleanValue onlyReach = new BooleanValue("Only reduce reach hits", this, false);
    
    @EventLink
    private Listener<AttackEvent> attack = event -> {
    	 if (this.isEnabled()) {
             if (onlyReach.getValue() && !mc.thePlayer.capabilities.isCreativeMode) {
                 final double dist = mc.objectMouseOver.hitVec.distanceTo(mc.getRenderViewEntity().getPositionEyes(1.0f));
                 double val;
                 if (dist > 3.0) {
                     val = (100.0 - slow.getValue()) / 100.0;
                 }
                 else {
                     val = 0.6;
                 }
                 mc.thePlayer.motionX *= val;
                 mc.thePlayer.motionZ *= val;
             }
             else {
                 final double dist = (100.0 - slow.getValue()) / 100.0;
                 mc.thePlayer.motionX *= dist;
                 mc.thePlayer.motionZ *= dist;
             }
         } else {
         	mc.thePlayer.motionX *= 0.6;
             mc.thePlayer.motionZ *= 0.6;
         }
    };
}
