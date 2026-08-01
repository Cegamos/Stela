package keystrokesmod.client.module.modules.client;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.ModeValue;

@ModuleInfo(name = "Gui", category = Category.Client, key = 54)
public class GuiModule extends Mod {

	public final ModeValue mode = new ModeValue("Mode", this, Colors.PastelPink, Colors.values());

    @Override
    public void onEnable() {
    	super.onEnable();
    	this.withEnabled(false);
        mc.displayGuiScreen(Kevin.clickGui);
    }

    public enum Colors {
        Mai, Sassan, Gold, Steel, Emerald, Orange, Amethyst, Lily, PastelPink, Cherry
    }
}
