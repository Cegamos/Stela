package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.NumberValue;

@ModuleInfo(name = "VClip", category = Category.Movement)
public class VClip extends Mod {
    private final NumberValue a = new NumberValue("Distace", this, 2.0, -10.0, 10.0, 0.5);

    @Override
    public void onEnable() {
        if (a.getValue() != 0.0) {
            mc.thePlayer.setPosition(mc.thePlayer.posX, mc.thePlayer.posY + a.getValue(), mc.thePlayer.posZ);
        }
        this.disable();
    }
}
