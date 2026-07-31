package keystrokesmod.client.module.modules.combat;

import java.util.HashSet;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.combat.Rotation;
import keystrokesmod.client.util.combat.RotationUtil;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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
    
    private final HashSet<String> friends = new HashSet<>();
    
    private final Rotation currentRot = new Rotation(0f, 0f);
    private final Rotation targetRot = new Rotation(0f, 0f);

    @Override
    public void update() {
        if (checkScreen() || checkGame()) return;
        
        if (breakBlocks.getValue() && mc.objectMouseOver != null) {
            final BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null) {
                final Block bl = getWorld().getBlockState(p).getBlock();
                if (bl != Blocks.air && !(bl instanceof BlockLiquid)) {
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
        final ThreadLocalRandom random = ThreadLocalRandom.current();
        
        currentRot.setYaw(getPlayer().rotationYaw);
        currentRot.setPitch(getPlayer().rotationPitch);
        targetRot.setYaw(currentRot.getYaw());
        targetRot.setPitch(currentRot.getPitch());

        final double n = Utils.Player.fovFromEntity(en);
        if (n > 1.0 || n < -1.0) {
            final double complimentSpeed = n * (random.nextDouble(compliment.getValue() - 1.47328, compliment.getValue() + 2.48293) / 100.0);
            final double val3 = -(complimentSpeed + n / (101.0 - random.nextDouble(speed.getValue() - 4.723847, speed.getValue())));
            
            final double mag = Math.abs(val3);
            final float stepYaw = (float) (mag > max ? (val3 > 0 ? max : -max) : val3);
            targetRot.setYaw(currentRot.getYaw() + stepYaw);
        }

        if (vertical.getValue()) {
            final float[] rotations = Utils.Player.getTargetRotations(en);
            if (rotations != null) {
                final double p = MathHelper.wrapAngleTo180_float(rotations[1] - currentRot.getPitch());
                if (p > 1.0 || p < -1.0) {
                    final double pCompliment = p * (random.nextDouble(verticalCompliment.getValue() - 1.0, verticalCompliment.getValue() + 1.5) / 100.0);
                    final double pStep = pCompliment + p / (101.0 - random.nextDouble(verticalSpeed.getValue() - 4.0, verticalSpeed.getValue()));
                    
                    final double pMag = Math.abs(pStep);
                    final float stepPitch = (float) (pMag > max ? (pStep > 0 ? max : -max) : pStep);
                    targetRot.setPitch(currentRot.getPitch() + stepPitch);
                }
            }
        }

        RotationUtil.applyMouseSensTarget(targetRot, currentRot);

        getPlayer().rotationYaw = targetRot.getYaw();
        getPlayer().rotationPitch = targetRot.getPitch();
    }

    public boolean isAFriend(final Entity entity) {
        if (entity == getPlayer()) return true;
        
        if (friends.contains(entity.getName().toLowerCase())) return true;
        
        if (entity instanceof EntityPlayer) {
            if (getPlayer().isOnSameTeam((EntityLivingBase) entity)) {
                return true;
            }
        }
        return false;
    }

    public Entity getEnemy() {
        final int fovInt = (int) this.fov.getValue();
        final double distSq = distance.getValue() * distance.getValue();
        Entity best = null;
        double bestScore = Double.MAX_VALUE;

        for (final EntityPlayer en : getWorld().playerEntities) {
            if (en == getPlayer() || en.isDead || en.isPlayerSleeping() || en.getHealth() <= 0.0f) continue;
            if (!aimInvis.getValue() && en.isInvisible()) continue;
            if (ignoreFriends.getValue() && isAFriend(en)) continue;
            
            final double dist = getPlayer().getDistanceSqToEntity(en);
            if (dist > distSq) continue;
            
            if (!blatantMode.getValue() && !Utils.Player.fov(en, fovInt)) continue;
            
            if (visibleOnly.getValue() && !isVisible(en)) continue;

            final double score;
            if (targetSort.is("Health")) {
                score = en.getHealth();
            } else if (targetSort.is("Angle")) {
                score = Math.abs(Utils.Player.fovFromEntity(en));
            } else {
                score = dist;
            }

            if (score < bestScore) {
                bestScore = score;
                best = en;
            }
        }
        return best;
    }

    private boolean isVisible(Entity target) {
        if (target == null) return false;
        final Vec3 eyes = new Vec3(posX(), posY() + getPlayer().getEyeHeight(), posZ());
        final Vec3 targetEyes = new Vec3(target.posX, target.posY + target.getEyeHeight(), target.posZ);
        final MovingObjectPosition hit = getWorld().rayTraceBlocks(eyes, targetEyes, false, true, false);
        
        if (hit == null) return true;
        return eyes.distanceTo(hit.hitVec) >= eyes.distanceTo(targetEyes) - 1.0;
    }

    public void addFriend(final String name) {
        friends.add(name.toLowerCase());
    }

    public void removeFriend(final String name) {
        friends.remove(name.toLowerCase());
    }
    
    public HashSet<String> getFriends() {
        return friends;
    }
}