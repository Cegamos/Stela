package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;

@ModuleInfo(name = "NoSlow", category = Category.Movement)
public class NoSlow extends Mod {
    private final DescriptionValue a = new DescriptionValue("Default is 80% motion reduction.", this);
    private final DescriptionValue c = new DescriptionValue("Hypixel max: 22%", this);
    public final NumberValue b = new NumberValue("Slow %", this, 80.0, 0.0, 80.0, 1.0);
}
