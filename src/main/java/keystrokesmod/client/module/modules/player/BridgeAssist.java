package keystrokesmod.client.module.modules.player;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PrePlayerTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "BridgeAssist", category = Category.Player)
public class BridgeAssist extends Mod {
    private final NumberValue edgeOffset = new NumberValue("Edge offset", this, 0.12, 0.05, 0.3, 0.01);
    private final NumberValue releaseDelay = new NumberValue("Release delay (ms)", this, 40.0, 0.0, 200.0, 5.0);
    private final BooleanValue pitchCheck = new BooleanValue("Only look down", this, true);
    private final BooleanValue backwardsOnly = new BooleanValue("Backwards only", this, true);
    private final BooleanValue blocksOnly = new BooleanValue("Holding blocks only", this, true);
    private final BooleanValue rightClickOnly = new BooleanValue("Right click only", this, true);

    private boolean sneaking;
    private long releaseTime;

    @Override
    public void onDisable() {
        super.onDisable();
        if (sneaking) {
            unpressSneak();
            sneaking = false;
        }
    }

    @EventLink
    private Listener<PrePlayerTickEvent> onPrePlayerTick = event -> {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
            if (sneaking) {
                unpressSneak();
                sneaking = false;
            }
            return;
        }

        if (!canBridgeAssist()) {
            if (sneaking) {
                unpressSneak();
                sneaking = false;
            }
            return;
        }

        boolean onEdge = isNearEdge();

        if (onEdge) {
            sneaking = true;
            pressSneak();
            releaseTime = 0L;
        } else if (sneaking) {
            if (releaseTime == 0L) {
                releaseTime = System.currentTimeMillis() + (long) (releaseDelay.getValue() + Math.random() * 20);
            }
            if (System.currentTimeMillis() >= releaseTime) {
                unpressSneak();
                sneaking = false;
                releaseTime = 0L;
            } else {
                pressSneak();
            }
        }
    };

    private boolean canBridgeAssist() {
        if (!mc.thePlayer.onGround) return false;

        if (rightClickOnly.getValue() && !Mouse.isButtonDown(1)) {
            return false;
        }

        if (pitchCheck.getValue() && mc.thePlayer.rotationPitch < 65.0f) {
            return false;
        }

        if (backwardsOnly.getValue() && mc.thePlayer.moveForward >= -0.1f) {
            return false;
        }

        if (blocksOnly.getValue()) {
            ItemStack item = mc.thePlayer.getHeldItem();
            if (item == null || !(item.getItem() instanceof ItemBlock)) {
                return false;
            }
        }

        return true;
    }

    private boolean isNearEdge() {
        double offset = edgeOffset.getValue();
        double predictedX = mc.thePlayer.posX + mc.thePlayer.motionX * 1.5;
        double predictedZ = mc.thePlayer.posZ + mc.thePlayer.motionZ * 1.5;
        double y = mc.thePlayer.posY - 1.0;

        BlockPos posUnder = new BlockPos(predictedX, y, predictedZ);
        Block block = mc.theWorld.getBlockState(posUnder).getBlock();

        if (block instanceof BlockAir) {
            return true;
        }

        double minX = mc.thePlayer.getEntityBoundingBox().minX - offset;
        double maxX = mc.thePlayer.getEntityBoundingBox().maxX + offset;
        double minZ = mc.thePlayer.getEntityBoundingBox().minZ - offset;
        double maxZ = mc.thePlayer.getEntityBoundingBox().maxZ + offset;

        BlockPos p1 = new BlockPos(minX, y, minZ);
        BlockPos p2 = new BlockPos(maxX, y, maxZ);

        return mc.theWorld.getBlockState(p1).getBlock() instanceof BlockAir ||
               mc.theWorld.getBlockState(p2).getBlock() instanceof BlockAir;
    }

    private void pressSneak() {
        int key = mc.gameSettings.keyBindSneak.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
    }

    private void unpressSneak() {
        int key = mc.gameSettings.keyBindSneak.getKeyCode();
        KeyBinding.setKeyBindState(key, false);
    }
}
