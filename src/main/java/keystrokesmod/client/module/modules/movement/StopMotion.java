package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;

@ModuleInfo(name = "StopMotion", category = Category.Movement)
public class StopMotion extends Mod {
    private final TickSetting a = new TickSetting("Stop X", this, true);
    private final TickSetting b = new TickSetting("Stop Y", this, true);
    private final TickSetting c = new TickSetting("Stop Z", this, true);
    
    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }
        
        if (a.isToggled()) mc.thePlayer.motionX = 0.0;
        if (b.isToggled()) mc.thePlayer.motionY = 0.0;
        if (c.isToggled()) mc.thePlayer.motionZ = 0.0;
        
        this.disable();
    }
}
