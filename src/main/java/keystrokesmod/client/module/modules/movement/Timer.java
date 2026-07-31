package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.system.ReflectUtil;

@ModuleInfo(name = "Timer", category = Category.Movement)
public class Timer extends Mod {
    private final NumberValue a = new NumberValue("Speed", this, 1.0, 0.5, 2.5, 0.01);
    private final BooleanValue b = new BooleanValue("Strafe only", this, false);
    
    @Override
    public void update() {
        if (!(mc.currentScreen instanceof ClickGui)) {
            if (b.isToggled() && mc.thePlayer.moveStrafing == 0.0f) {
                ReflectUtil.resetTimer();
                return;
            }
            ReflectUtil.getTimer().timerSpeed = (float)a.getInput();
        }
        else {
            ReflectUtil.resetTimer();
        }
    }
    
    @Override
    public void onDisable() {
        ReflectUtil.resetTimer();
    }
}
