package keystrokesmod.client.module.modules.client;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.KeyEvent;

@ModuleInfo(name = "Gui", category = Category.Client, key = 54)
public class GuiModule extends Mod {

	public final ModeValue mode = new ModeValue("Mode", this, Colors.PastelPink, Colors.values());
	
	public GuiModule() {
		this.withEnabled(false);
	}

    @Override
    public void onEnable() {
        mc.displayGuiScreen(Raven.clickGui);
        super.onEnable();
    }

    @EventLink
    public final Listener<KeyEvent> onInputKeyboard = event -> {
        if (Keyboard.getEventKey() == this.keycode || Keyboard.getEventKey() == Keyboard.KEY_ESCAPE) {
            if (mc.currentScreen == null) {
            	mc.displayGuiScreen(Raven.clickGui);
                this.disable();
                this.onDisable();
            } else {
                mc.displayGuiScreen(null);
                this.disable();
                this.onDisable();
            }
        }
    };

    @Override
    public void onDisable() {
    	mc.displayGuiScreen(null);
    	super.onDisable();
    }

    public enum Colors {
        Mai, Sassan, Gold, Steel, Emerald, Orange, Amethyst, Lily, PastelPink, Cherry
    }
}
