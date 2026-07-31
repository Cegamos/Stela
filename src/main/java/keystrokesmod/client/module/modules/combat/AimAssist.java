package keystrokesmod.client.module.modules.combat;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.world.AntiBot;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.utils.timing.Clock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "AimAssist", category = Category.Combat)
public class AimAssist extends Mod {

	private final SliderSetting speed = new SliderSetting("Speed 1", this, 45.0, 5.0, 100.0, 1.0);
	private final SliderSetting compliment = new SliderSetting("Speed 2", this, 15.0, 2.0, 97.0, 1.0);
	private final SliderSetting fov = new SliderSetting("FOV", this, 90.0, 15.0, 360.0, 1.0);
	private final SliderSetting distance = new SliderSetting("Distance", this, 4.5, 1.0, 10.0, 0.5);
	private final TickSetting clickAim = new TickSetting("Click aim", this, true);
	private final TickSetting weaponOnly = new TickSetting("Weapon only", this, false);
	private final TickSetting aimInvis = new TickSetting("Aim invis", this, false);
	private final TickSetting breakBlocks = new TickSetting("Break blocks", this, true);
	private final TickSetting blatantMode = new TickSetting("Blatant mode", this, false);
	private final TickSetting ignoreFriends = new TickSetting("Ignore Friends", this, true);

	private final Clock clock = new Clock(0);
	private final ArrayList<Entity> friends = new ArrayList<Entity>();

	@Override
	public void update() {
		if (!Utils.Client.currentScreenMinecraft()) {
			return;
		}
		if (!Utils.Player.isPlayerInGame()) {
			return;
		}
		if (breakBlocks.isToggled() && mc.objectMouseOver != null) {
			final BlockPos p = mc.objectMouseOver.getBlockPos();
			if (p != null) {
				final Block bl = mc.theWorld.getBlockState(p).getBlock();
				if (bl != Blocks.air && !(bl instanceof BlockLiquid) && bl instanceof Block) {
					return;
				}
			}
		}

		if (mc.gameSettings.keyBindAttack.isKeyDown()) clock.start();
		if (clickAim.isToggled() && (clock.finished(150) || !mc.thePlayer.isSwingInProgress)) return;

		if (!weaponOnly.isToggled() || Utils.Player.isPlayerHoldingWeapon()) {
			final Entity en = this.getEnemy();
			if (en != null) {
				if (blatantMode.isToggled()) {
					Utils.Player.aim(en, 0.0f, false);
				} else {
					final double n = Utils.Player.fovFromEntity(en);
					if (n > 1.0 || n < -1.0) {
						final double complimentSpeed = n * (ThreadLocalRandom.current().nextDouble(compliment.getInput() - 1.47328, compliment.getInput() + 2.48293) / 100.0);
						final float val3 = (float) (-(complimentSpeed + n / (101.0 - (float) ThreadLocalRandom.current().nextDouble(speed.getInput() - 4.723847, speed.getInput()))));
						mc.thePlayer.rotationYaw += val3;
					}
				}
			}
		}

	}

	public boolean isAFriend(final Entity entity) {
		if (entity == mc.thePlayer) {
			return true;
		}
		for (final Entity wut : friends) {
			if (wut.equals((Object) entity)) {
				return true;
			}
		}
		try {
			final EntityPlayer bruhentity = (EntityPlayer) entity;
			if (mc.thePlayer.isOnSameTeam((EntityLivingBase) entity)
					|| mc.thePlayer.getDisplayName().getUnformattedText()
							.startsWith(bruhentity.getDisplayName().getUnformattedText().substring(0, 2))) {
				return true;
			}
		} catch (Exception fhwhfhwe) {
			if (Raven.debugger) {
				Utils.Player.sendMessageToSelf(fhwhfhwe.getMessage());
			}
		}
		return false;
	}

	public Entity getEnemy() {
		AntiBot bot = (AntiBot) Raven.moduleManager.getModuleByClazz(AntiBot.class);
		final int fov = (int) this.fov.getInput();
		for (final EntityPlayer en : mc.theWorld.playerEntities) {
			if ((!ignoreFriends.isToggled() || !isAFriend((Entity) en)) && en != mc.thePlayer && !en.isDead
					&& (aimInvis.isToggled() || !en.isInvisible())
					&& mc.thePlayer.getDistanceToEntity((Entity) en) <= distance.getInput() && !bot.bot((Entity) en)
					&& (blatantMode.isToggled() || Utils.Player.fov(en, fov))) {
				return (Entity) en;
			}
		}
		return null;
	}

	public void addFriend(final Entity entityPlayer) {
		friends.add(entityPlayer);
	}

	public boolean addFriend(final String name) {
		boolean found = false;
		for (final Entity entity : mc.theWorld.getLoadedEntityList()) {
			if ((entity.getName().equalsIgnoreCase(name) || entity.getCustomNameTag().equalsIgnoreCase(name))
					&& !isAFriend(entity)) {
				addFriend(entity);
				found = true;
			}
		}
		return found;
	}

	public boolean removeFriend(final String name) {
		boolean removed = false;
		boolean found = false;
		for (final NetworkPlayerInfo networkPlayerInfo : new ArrayList<NetworkPlayerInfo>(
				mc.getNetHandler().getPlayerInfoMap())) {
			final Entity entity = (Entity) mc.theWorld
					.getPlayerEntityByName(networkPlayerInfo.getDisplayName().getUnformattedText());
			if (entity.getName().equalsIgnoreCase(name) || entity.getCustomNameTag().equalsIgnoreCase(name)) {
				removed = removeFriend(entity);
				found = true;
			}
		}
		return found && removed;
	}

	public boolean removeFriend(final Entity entityPlayer) {
		try {
			friends.remove(entityPlayer);
		} catch (Exception eeeeee) {
			eeeeee.printStackTrace();
			return false;
		}
		return true;
	}

	public ArrayList<Entity> getFriends() {
		return friends;
	}
}
