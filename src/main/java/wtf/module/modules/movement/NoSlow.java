package wtf.module.modules.movement;

import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.DescriptionValue;
import wtf.module.value.impl.NumberValue;

@ModuleInfo(name = "NoSlow", category = Category.Movement)
public class NoSlow extends Mod {
    private final DescriptionValue a = new DescriptionValue("Default is 80% motion reduction.", this);
    private final DescriptionValue c = new DescriptionValue("Hypixel max: 22%", this);
    public final NumberValue b = new NumberValue("Slow %", this, 80.0, 0.0, 80.0, 1.0);
}
