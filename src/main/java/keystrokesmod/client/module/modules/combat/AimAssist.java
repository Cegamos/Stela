package keystrokesmod.client.module.modules.combat;

import java.util.ArrayList;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "AimAssist", category = Category.Combat)
public class AimAssist extends Mod {

	private final NumberValue speed = new NumberValue("Speed 1", this, 45.0, 5.0, 100.0, 1.0);
	private final NumberValue compliment = new NumberValue("Speed 2", this, 15.0, 2.0, 97.0, 1.0);
	private final NumberValue fov = new NumberValue("FOV", this, 90.0, 15.0, 360.0, 1.0);
	private final NumberValue distance = new NumberValue("Distance", this, 4.5, 1.0, 10.0, 0.5);
	private final BooleanValue clickAim = new BooleanValue("Click aim", this, true);
	private final BooleanValue weaponOnly = new BooleanValue("Weapon only", this, false);
	private final BooleanValue aimInvis = new BooleanValue("Aim invis", this, false);
	private final BooleanValue breakBlocks = new BooleanValue("Break blocks", this, true);
	private final BooleanValue blatantMode = new BooleanValue("Blatant mode", this, false);
	private final BooleanValue ignoreFriends = new BooleanValue("Ignore Friends", this, true);

	private final BooleanValue vertical = new BooleanValue("Vertical", this, true);
	private final NumberValue verticalSpeed = new NumberValue("Vertical speed", this, 20.0, 5.0, 100.0, 1.0);
	private final NumberValue verticalCompliment = new NumberValue("Vertical speed 2", this, 10.0, 2.0, 50.0, 1.0);
	private final ModeValue targetSort = new ModeValue("Target sort", this, "Closest", "Closest", "Health", "Angle");
	private final BooleanValue visibleOnly = new BooleanValue("Visible only", this, false);
	private final NumberValue maxAngle = new NumberValue("Max angle", this, 6.0, 1.0, 90.0, 0.5);

	private final Clock clock = new Clock(0);
	private final ArrayList<Entity> friends = new ArrayList<Entity>();

	@Override
	public void update() {
		if (checkScreen()) return;
		if (checkGame()) return;
		
		if (breakBlocks.getValue() && mc.objectMouseOver != null) {
			final BlockPos p = mc.objectMouseOver.getBlockPos();
			if (p != null) {
				final Block bl = getWorld().getBlockState(p).getBlock();
				if (bl != Blocks.air && !(bl instanceof BlockLiquid) && bl instanceof Block) {
					return;
				}
			}
		}

		if (gameSetting().keyBindAttack.isKeyDown()) clock.start();
		if (clickAim.getValue() && (clock.finished(150) || !getPlayer().isSwingInProgress)) return;

		if (!weaponOnly.getValue() || Utils.Player.isPlayerHoldingWeapon()) {
			final Entity en = this.getEnemy();
			if (en != null) {
				this.assist(en);
			}
		}
	}

	private void assist(Entity en) {
		if (blatantMode.getValue()) {
			Utils.Player.aim(en, 0.0f, false);
			return;
		}

		final double max = maxAngle.getValue();

		final double n = Utils.Player.fovFromEntity(en);
		if (n > 1.0 || n < -1.0) {
			final double complimentSpeed = n * (ThreadLocalRandom.current().nextDouble(compliment.getValue() - 1.47328, compliment.getValue() + 2.48293) / 100.0);
			final double val3 = -(complimentSpeed + n / (101.0 - ThreadLocalRandom.current().nextDouble(speed.getValue() - 4.723847, speed.getValue())));
			final double mag = Math.abs(val3);
			if (mag > max) {
				getPlayer().rotationYaw += (val3 > 0 ? max : -max);
			} else {
				getPlayer().rotationYaw += val3;
			}
		}

		if (vertical.getValue()) {
			final float[] rotations = Utils.Player.getTargetRotations(en);
			if (rotations != null) {
				final double p = MathHelper.wrapAngleTo180_float(rotations[1] - getPlayer().rotationPitch);
				if (p > 1.0 || p < -1.0) {
					final double pCompliment = p * (ThreadLocalRandom.current().nextDouble(verticalCompliment.getValue() - 1.0, verticalCompliment.getValue() + 1.5) / 100.0);
					final double pStep = pCompliment + p / (101.0 - ThreadLocalRandom.current().nextDouble(verticalSpeed.getValue() - 4.0, verticalSpeed.getValue()));
					final double pMag = Math.abs(pStep);
					final float step = (float) (pMag > max ? (pStep > 0 ? max : -max) : pStep);
					getPlayer().rotationPitch = MathHelper.clamp_float(getPlayer().rotationPitch + step, -90.0f, 90.0f);
				}
			}
		}
	}

	public boolean isAFriend(final Entity entity) {
		if (entity == getPlayer()) {
			return true;
		}
		for (final Entity wut : friends) {
			if (wut.equals((Object) entity)) {
				return true;
			}
		}
		try {
			final EntityPlayer bruhentity = (EntityPlayer) entity;
			if (getPlayer().isOnSameTeam((EntityLivingBase) entity)
					|| getPlayer().getDisplayName().getUnformattedText()
							.startsWith(bruhentity.getDisplayName().getUnformattedText().substring(0, 2))) {
				return true;
			}
		} catch (Exception fhwhfhwe) { }
		return false;
	}

	public Entity getEnemy() {
		final int fov = (int) this.fov.getValue();
		Entity best = null;
		double bestScore = Double.MAX_VALUE;
		for (final EntityPlayer en : getWorld().playerEntities) {
			if (en == getPlayer() || en.isDead || en.isPlayerSleeping()) {
				continue;
			}
			if (en.getHealth() <= 0.0f) {
				continue;
			}
			if (!aimInvis.getValue() && en.isInvisible()) {
				continue;
			}
			if (ignoreFriends.getValue() && isAFriend((Entity) en)) {
				continue;
			}
			final double dist = getPlayer().getDistanceToEntity((Entity) en);
			if (dist > distance.getValue()) {
				continue;
			}
			if (!blatantMode.getValue() && !Utils.Player.fov(en, fov)) {
				continue;
			}
			if (visibleOnly.getValue() && !isVisible((Entity) en)) {
				continue;
			}

			final double score;
			if (targetSort.is("Health")) {
				score = en.getHealth();
			} else if (targetSort.is("Angle")) {
				score = Math.abs(Utils.Player.fovFromEntity((Entity) en));
			} else {
				score = dist;
			}

			if (score < bestScore) {
				bestScore = score;
				best = (Entity) en;
			}
		}
		return best;
	}

	private boolean isVisible(Entity target) {
		if (target == null) {
			return false;
		}
		final Vec3 eyes = new Vec3(posX(), posY() + getPlayer().getEyeHeight(), posZ());
		final Vec3 targetEyes = new Vec3(target.posX, target.posY + target.getEyeHeight(), target.posZ);
		final MovingObjectPosition hit = getWorld().rayTraceBlocks(eyes, targetEyes, false, true, false);
		if (hit == null) {
			return true;
		}
		final double hitDist = eyes.distanceTo(hit.hitVec);
		final double targetDist = eyes.distanceTo(targetEyes);
		return hitDist >= targetDist - 1.0;
	}

	public void addFriend(final Entity entityPlayer) {
		friends.add(entityPlayer);
	}

	public boolean addFriend(final String name) {
		boolean found = false;
		for (final Entity entity : getWorld().getLoadedEntityList()) {
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
			final Entity entity = (Entity) getWorld()
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
