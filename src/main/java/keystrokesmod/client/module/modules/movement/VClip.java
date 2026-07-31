package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.SliderSetting;

@ModuleInfo(name = "VClip", category = Category.Movement)
public class VClip extends Mod {
    private final SliderSetting a = new SliderSetting("Distace", this, 2.0, -10.0, 10.0, 0.5);

    @Override
    public void onEnable() {
        if (a.getInput() != 0.0) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + a.getInput(), mc.thePlayer.posZ);
        }
        this.disable();
    }
}
