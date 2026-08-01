package wtf.module.modules.client;

import wtf.Kevin;
import wtf.clickgui.ClickGui;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PreTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.util.Utils;
import wtf.util.render.animation.TimeAnimation;

@ModuleInfo(name = "Terminal", category = Category.Client, enabled = true)
public class Terminal extends Mod {
	public static boolean visible = false;
	public static boolean b = false;
	public static TimeAnimation animation;
	public static BooleanValue animate;
	
	@Override
	public void onEnable() {
		super.onEnable();
		Kevin.clickGui.terminal.show();
		(animation = new TimeAnimation(500.0f)).start();
	}

	@EventLink
	public final Listener<PreTickEvent> onTick = e -> {
		if (Utils.Player.isPlayerInGame() && this.enabled && mc.currentScreen instanceof ClickGui
				&& Kevin.clickGui.terminal.hidden()) {
			Kevin.clickGui.terminal.show();
		}
	};

	@Override
	public void onDisable() {
		super.onDisable();
		Kevin.clickGui.terminal.hide();
		if (Terminal.animation != null) {
			Terminal.animation.start();
		}
	}
}
