package wtf.module.modules.client;

import wtf.Kevin;
import wtf.event.EventBus;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.util.chat.ChatUtil;

@ModuleInfo(name = "Self Destruct", category = Category.Client)
public class SelfDestruct extends Mod {
    @Override
    public void onEnable() {
        this.disable();
        SelfDestruct.mc.displayGuiScreen(null);
        for (final Mod module : Kevin.moduleManager.getModules()) {
            if (module != this && module.isEnabled()) {
                module.disable();
            }
        }
        EventBus.INSTANCE.unregister(new Kevin());
        EventBus.INSTANCE.unregister(new ChatUtil());
    }
}
