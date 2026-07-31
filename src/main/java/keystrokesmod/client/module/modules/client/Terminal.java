package keystrokesmod.client.module.modules.client;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.animations.TimeAnimation;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PreTickEvent;

@ModuleInfo(name = "Terminal", category = Category.Client, enabled = true)
public class Terminal extends Mod {
	public static boolean visible = false;
	public static boolean b = false;
	public static TimeAnimation animation;
	public static BooleanValue animate;
	
	@Override
	public void onEnable() {
		super.onEnable();
		Raven.clickGui.terminal.show();
		(animation = new TimeAnimation(500.0f)).start();
	}

	@EventLink
	public final Listener<PreTickEvent> onTick = e -> {
		if (Utils.Player.isPlayerInGame() && this.enabled && mc.currentScreen instanceof ClickGui
				&& Raven.clickGui.terminal.hidden()) {
			Raven.clickGui.terminal.show();
		}
	};

	@Override
	public void onDisable() {
		super.onDisable();
		Raven.clickGui.terminal.hide();
		if (Terminal.animation != null) {
			Terminal.animation.start();
		}
	}
}
