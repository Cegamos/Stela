package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;

@ModuleInfo(name = "NoSlow", category = Category.Movement)
public class NoSlow extends Mod {
    private final DescriptionSetting a = new DescriptionSetting("Default is 80% motion reduction.", this);
    private final DescriptionSetting c = new DescriptionSetting("Hypixel max: 22%", this);
    public final SliderSetting b = new SliderSetting("Slow %", this, 80.0, 0.0, 80.0, 1.0);
}
