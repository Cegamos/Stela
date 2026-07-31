package keystrokesmod.client.module.modules.other;

import java.awt.Color;
import java.util.ArrayList;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.event.impl.RenderWorldLastEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.render.RenderUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBow;
import net.minecraft.util.EnumChatFormatting;

@ModuleInfo(name = "MurderMystery", category = Category.Other)
public class MurderMystery extends Mod {
	private final BooleanValue alertMurder = new BooleanValue("Alert murder", this, true);
	private final BooleanValue alertDetective = new BooleanValue("Alert detective", this, true);

	private final BooleanValue drawMurder = new BooleanValue("Draw murder esp", this, false);
	private final BooleanValue drawDetective = new BooleanValue("Draw detective esp", this, false);

	private final ArrayList<EntityPlayer> murderers = new ArrayList<>();
	private final ArrayList<EntityPlayer> detectives = new ArrayList<>();
	
	@Override
	public void onDisable() {
		murderers.clear();
		detectives.clear();
	}
	
	@EventLink
	public final Listener<PreTickEvent> onTick = event -> {
		if (!Utils.Player.isPlayerInGame()) {
			murderers.clear();
			detectives.clear();
			return;
		}

		for (EntityPlayer player : mc.theWorld.playerEntities) {
			if (player.getHeldItem() == null || detectives.contains(player) || player == mc.thePlayer) continue;
			String itemName = player.getHeldItem().getDisplayName();

			if (!murderers.contains(player) && isMurder(itemName)) {
				murderers.add(player);
				mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
				sendNotification(player.getName() + " es el asesino.", EnumChatFormatting.RED, "!");
			}

			if (isDetective(player) && !detectives.contains(player)) {
				detectives.add(player);
				mc.thePlayer.playSound("note.pling", 1.0F, 1.0F);
				sendNotification(player.getName() + " es el detective.", EnumChatFormatting.BLUE, "!");
			}
		}
	};

	@EventLink
	public final Listener<RenderWorldLastEvent> onRender = event -> {
		if (!Utils.Player.isPlayerInGame()) return;

		for (Entity entity : mc.theWorld.loadedEntityList) {
			if (entity instanceof EntityItem) {
				String name = ((EntityItem) entity).getEntityItem().getDisplayName();

				if (name.equalsIgnoreCase("gold ingot")) {
					RenderUtil.drawSimpleItemBox(entity, Color.YELLOW);
				} else if (name.equalsIgnoreCase("bow")) {
					RenderUtil.drawSimpleItemBox(entity, Color.CYAN);
				}
			} else if (entity instanceof EntityPlayer && entity != mc.thePlayer) {
				if (murderers.contains(entity) && drawMurder.getValue()) {
					RenderUtil.drawSimpleBox((EntityPlayer) entity, Color.RED.getRGB(), event.getPartialTicks());
				} else if (detectives.contains(entity) && drawDetective.getValue()) {
					RenderUtil.drawSimpleBox((EntityPlayer) entity, Color.BLUE.getRGB(), event.getPartialTicks());
				}
			}
		}
	};

	private void sendNotification(String message, EnumChatFormatting color, String symbol) {
		Utils.Player.sendMessageToSelf(color + symbol + " " + EnumChatFormatting.RESET + message);
	}

	private boolean isMurder(String itemName) {
		return itemName.contains("Knife") || itemName.contains("Sword") || itemName.contains("Scythe");
	}

	private boolean isDetective(EntityPlayer player) {
		return player.getHeldItem() != null && player.getHeldItem().getItem() instanceof ItemBow;
	}
}
