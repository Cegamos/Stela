package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.utils.system.ReflectUtil;

@ModuleInfo(name = "Timer", category = Category.Movement)
public class Timer extends Mod {
    private final SliderSetting a = new SliderSetting("Speed", this, 1.0, 0.5, 2.5, 0.01);
    private final TickSetting b = new TickSetting("Strafe only", this, false);
    
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
