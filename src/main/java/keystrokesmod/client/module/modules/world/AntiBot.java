package keystrokesmod.client.module.modules.world;

import java.util.HashMap;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.player.Freecam;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "AntiBot", category = Category.World, enabled = true)
public class AntiBot extends Mod {
	private static final HashMap<EntityPlayer, Long> newEnt;
	private final long ms = 4000L;
	public TickSetting a = new TickSetting("Wait 80 ticks", this, false);

	@Override
	public void onDisable() {
		newEnt.clear();
	}

	@SubscribeEvent
	public void onEntityJoinWorld(final EntityJoinWorldEvent event) {
		if (!Utils.Player.isPlayerInGame()) {
			return;
		}
		if (a.isToggled() && event.entity instanceof EntityPlayer && event.entity != mc.thePlayer) {
			newEnt.put((EntityPlayer) event.entity, System.currentTimeMillis());
		}
	}

	@Override
	public void update() {
		if (a.isToggled() && !newEnt.isEmpty()) {
			final long now = System.currentTimeMillis();
			newEnt.values().removeIf(e -> e < now - 4000L);
		}
	}

	public boolean bot(final Entity en) {
		if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
			return false;
		}
		if (Freecam.fakePlayer != null && Freecam.fakePlayer == en) {
			return true;
		}
		final Mod antiBot = Raven.moduleManager.getModuleByClazz(AntiBot.class);
		if (antiBot != null && !antiBot.isEnabled()) {
			return false;
		}
		if (!Utils.Client.isHyp()) {
			return false;
		}
		if (a.isToggled() && !newEnt.isEmpty() && newEnt.containsKey(en)) {
			return true;
		}
		if (en.getName().startsWith("�c")) {
			return true;
		}
		final String n = en.getDisplayName().getUnformattedText();
		if (n.contains("�")) {
			return n.contains("[NPC] ");
		}
		if (n.isEmpty() && en.getName().isEmpty()) {
			return true;
		}
		if (n.length() == 10) {
			int num = 0;
			int let = 0;
			final char[] charArray;
			final char[] var4 = charArray = n.toCharArray();
			for (final char c : charArray) {
				if (Character.isLetter(c)) {
					if (Character.isUpperCase(c)) {
						return false;
					}
					++let;
				} else {
					if (!Character.isDigit(c)) {
						return false;
					}
					++num;
				}
			}
			return num >= 2 && let >= 2;
		}
		return false;
	}

	static {
		newEnt = new HashMap<EntityPlayer, Long>();
	}
}
