package wtf.module.modules.movement;

import wtf.clickgui.ClickGui;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.system.ReflectUtil;

@ModuleInfo(name = "Timer", category = Category.Movement)
public class Timer extends Mod {
    private final NumberValue a = new NumberValue("Speed", this, 1.0, 0.5, 2.5, 0.01);
    private final BooleanValue b = new BooleanValue("Strafe only", this, false);
    
    @Override
    public void update() {
        if (!(mc.currentScreen instanceof ClickGui)) {
            if (b.getValue() && mc.thePlayer.moveStrafing == 0.0f) {
                ReflectUtil.resetTimer();
                return;
            }
            ReflectUtil.getTimer().timerSpeed = (float)a.getValue();
        }
        else {
            ReflectUtil.resetTimer();
        }
    }
    
    @Override
    public void onDisable() {
    	super.onDisable();
        ReflectUtil.resetTimer();
    }
}
