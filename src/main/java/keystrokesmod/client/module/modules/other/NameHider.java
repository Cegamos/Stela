package keystrokesmod.client.module.modules.other;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.RenderTextEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.util.Utils;

@ModuleInfo(name = "NameHider", category = Category.Other)
public class NameHider extends Mod {
	public static String name = "";
	
	@EventLink
	public final Listener<RenderTextEvent> onRenderText = event -> {
		if (!Utils.Player.isPlayerInGame() || event.getText() == null) return;

		String text = event.getText();
		String ownName = mc.getSession().getUsername();
		String displayName = checkName();

		if (text.startsWith("/") || text.startsWith(".")) {
			return;
		}

		if (text.contains(ownName)) {
			text = text.replace(ownName, displayName);
			event.setText(text);
		}
	};
	
	public String checkName() {
		return name.isEmpty() ? "You" : name;
	}
}
