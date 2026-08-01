package keystrokesmod.client.module.modules.client;

import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.chat.ChatUtil;
import keystrokesmod.keystroke.KeyStrokeRenderer;

@ModuleInfo(name = "Self Destruct", category = Category.Client)
public class SelfDestruct extends Mod {
    @Override
    public void onEnable() {
        this.disable();
        SelfDestruct.mc.displayGuiScreen(null);
        for (final Mod module : Raven.moduleManager.getModules()) {
            if (module != this && module.isEnabled()) {
                module.disable();
            }
        }
        EventBus.INSTANCE.unregister(new Raven());
        EventBus.INSTANCE.unregister(new KeyStrokeRenderer());
        EventBus.INSTANCE.unregister(new ChatUtil());
    }
}
