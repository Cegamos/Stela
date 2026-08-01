package wtf.module.modules.player;

import org.lwjgl.input.Mouse;

import net.minecraft.block.BlockAir;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PreInputEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;

@ModuleInfo(name = "BridgeAssist", category = Category.Player)
public class BridgeAssist extends Mod {
    private final NumberValue edgeOffset = new NumberValue("Edge offset", this, 0.12, 0.05, 0.3, 0.01);
    private final NumberValue releaseDelay = new NumberValue("Release delay", this, 40.0, 0.0, 200.0, 5.0);
    private final BooleanValue pitchCheck = new BooleanValue("Only look down", this, true);
    private final BooleanValue backwardsOnly = new BooleanValue("Backwards only", this, true);
    private final BooleanValue blocksOnly = new BooleanValue("Holding blocks only", this, true);
    private final BooleanValue rightClickOnly = new BooleanValue("Right click only", this, true);

    private final BooleanValue sneakOnJump = new BooleanValue("Sneak on jump", this, true);

    private boolean sneaking;
    private long releaseTime;

    @EventLink
    private Listener<PreInputEvent> onPrePlayerTick = event -> {
        if (checkGame() || currentScreen() != null) {
            if (sneaking) {
                event.setSneak(false);
                sneaking = false;
            }
            return;
        }

        if (!canBridgeAssist()) {
            if (sneaking) {
            	event.setSneak(false);
                sneaking = false;
            }
            return;
        }

        if (!getPlayer().onGround && sneakOnJump.getValue()) {
            sneaking = true;
            event.setSneak(true);
            releaseTime = 0L;
            return;
        }

        boolean onEdge = isNearEdge();

        if (onEdge) {
            sneaking = true;
            event.setSneak(true);
            releaseTime = 0L;
        } else if (sneaking) {
            if (releaseTime == 0L) {
                releaseTime = System.currentTimeMillis() + (long) (releaseDelay.getValue() + Math.random() * 20);
            }
            if (System.currentTimeMillis() >= releaseTime) {
            	event.setSneak(false);
                sneaking = false;
                releaseTime = 0L;
            } else {
            	event.setSneak(true);
            }
        }
    };

    private boolean canBridgeAssist() {
        if (!getPlayer().onGround && (!sneakOnJump.getValue() || (getPlayer().moveForward == 0 && getPlayer().moveStrafing == 0))) return false;

        if (rightClickOnly.getValue() && !Mouse.isButtonDown(1)) {
            return false;
        }

        if (pitchCheck.getValue() && pitch() < 65.0f) {
            return false;
        }

        if (backwardsOnly.getValue() && getPlayer().moveForward >= -0.1f) {
            return false;
        }

        if (blocksOnly.getValue()) {
            ItemStack item = getPlayer().getHeldItem();
            if (item == null || !(item.getItem() instanceof ItemBlock)) {
                return false;
            }
        }

        return true;
    }

    private final double[][] radius = {{-0.3, -0.3}, {0.3, -0.3}, {-0.3, 0.3}, {0.3, 0.3}};

    private boolean isNearEdge() {
        Vec3 offset = predictMovement();
        Vec3 predictedPos = new Vec3(
                getPlayer().posX + getPlayer().motionX + offset.xCoord,
                getPlayer().posY,
                getPlayer().posZ + getPlayer().motionZ + offset.zCoord
        );
        Vec3 playerPos = new Vec3(getPlayer().posX, getPlayer().posY, getPlayer().posZ);

        double edge = computeEdge(predictedPos, playerPos);

        if (Double.isNaN(edge)) {
            return true;
        }

        return edge > edgeOffset.getValue();
    }

    private double computeEdge(Vec3 sim, Vec3 real) {
        int y = (int) (sim.yCoord - 0.01);
        double best = Double.NaN;
        for (double[] c : radius) {
            int bx = (int) Math.floor(real.xCoord + c[0]);
            int bz = (int) Math.floor(real.zCoord + c[1]);
            if (getWorld().getBlockState(new BlockPos(bx, y, bz)).getBlock() instanceof BlockAir) continue;
            double oX = Math.abs(sim.xCoord - (bx + (sim.xCoord < bx + 0.5 ? 0 : 1)));
            double oZ = Math.abs(sim.zCoord - (bz + (sim.zCoord < bz + 0.5 ? 0 : 1)));
            double dist = ((int) Math.floor(sim.xCoord) != bx)
                    ? (((int) Math.floor(sim.zCoord) != bz) ? Math.max(oX, oZ) : oX)
                    : (((int) Math.floor(sim.zCoord) != bz) ? oZ : 0);
            best = Double.isNaN(best) ? dist : Math.min(best, dist);
        }
        return best;
    }

    private Vec3 predictMovement() {
        int left = 0;
        if (mc.gameSettings.keyBindLeft.isKeyDown()) left++;
        if (mc.gameSettings.keyBindRight.isKeyDown()) left--;

        int forward = 0;
        if (mc.gameSettings.keyBindForward.isKeyDown()) forward++;
        if (mc.gameSettings.keyBindBack.isKeyDown()) forward--;

        float strafeInput = (float) left * 0.98f;
        float forwardInput = (float) forward * 0.98f;
        float inputMagnitude = strafeInput * strafeInput + forwardInput * forwardInput;

        if (inputMagnitude >= 1.0E-4f) {
            inputMagnitude = MathHelper.sqrt_float(inputMagnitude);
            if (inputMagnitude < 1.0f) {
                inputMagnitude = 1.0f;
            }

            float slipperiness = getWorld().getBlockState(new BlockPos(
                    MathHelper.floor_double(getPlayer().posX),
                    MathHelper.floor_double(getPlayer().getEntityBoundingBox().minY) - 1,
                    MathHelper.floor_double(getPlayer().posZ)
            )).getBlock().slipperiness * 0.91f;

            float speed = getPlayer().getAIMoveSpeed() * (0.16277136f / (slipperiness * slipperiness * slipperiness));

            inputMagnitude = speed / inputMagnitude;
            float sinYaw = MathHelper.sin(getPlayer().rotationYaw * (float) Math.PI / 180.0f);
            float cosYaw = MathHelper.cos(getPlayer().rotationYaw * (float) Math.PI / 180.0f);
            strafeInput *= inputMagnitude;
            forwardInput *= inputMagnitude;
            return new Vec3(strafeInput * cosYaw - forwardInput * sinYaw, 0.0, forwardInput * cosYaw + strafeInput * sinYaw);
        }
        return new Vec3(0.0, 0.0, 0.0);
    }
}
