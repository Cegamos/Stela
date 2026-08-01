package wtf.module.modules.client;

import wtf.Kevin;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;

@ModuleInfo(name = "Gui", category = Category.Client, key = 54)
public class GuiModule extends Mod {

    @Override
    public void onEnable() {
    	super.onEnable();
    	this.withEnabled(false);
        mc.displayGuiScreen(Kevin.clickGui);
    }
}
