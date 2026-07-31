package keystrokesmod.client.module.modules.player;

import java.awt.Color;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Freecam", category = Category.Player)
public class Freecam extends Mod {
    private final NumberValue speed = new NumberValue("Speed", this, 2.5, 0.5, 10.0, 0.5);
    private final BooleanValue disableOnDamage = new BooleanValue("Disable on damage", this, true);
    
    public static EntityOtherPlayerMP fakePlayer = null;
    private final float[] savedAngles = new float[2];
    private int[] lastChunkCoords = new int[] { Integer.MAX_VALUE, 0 };

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) return;

        fakePlayer = new EntityOtherPlayerMP(mc.theWorld, mc.thePlayer.getGameProfile());
        fakePlayer.copyLocationAndAnglesFrom(mc.thePlayer);
        fakePlayer.setInvisible(true);
        fakePlayer.setVelocity(0, 0, 0);
        savedAngles[0] = mc.thePlayer.rotationYawHead;
        savedAngles[1] = mc.thePlayer.rotationPitch;
        fakePlayer.rotationYawHead = savedAngles[0];
        mc.theWorld.addEntityToWorld(-8008, fakePlayer);
        mc.setRenderViewEntity(fakePlayer);
    }

    @Override
    public void onDisable() {
    	if (!Utils.Player.isPlayerInGame()) return;
        if (fakePlayer != null) {
            mc.setRenderViewEntity(mc.thePlayer);
            mc.thePlayer.rotationYaw = savedAngles[0];
            mc.thePlayer.rotationYawHead = savedAngles[0];
            mc.thePlayer.rotationPitch = savedAngles[1];
            mc.theWorld.removeEntity(fakePlayer);
            fakePlayer = null;
        }

        lastChunkCoords = new int[] { Integer.MAX_VALUE, 0 };

        int baseX = mc.thePlayer.chunkCoordX;
        int baseZ = mc.thePlayer.chunkCoordZ;
        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                int cx = baseX + dx;
                int cz = baseZ + dz;
                mc.theWorld.markBlockRangeForRenderUpdate(cx * 16, 0, cz * 16, cx * 16 + 15, 256, cz * 16 + 15);
            }
        }
    }

    @Override
    public void update() {
        if (!Utils.Player.isPlayerInGame() || fakePlayer == null || mc.thePlayer == null) return;

        if (disableOnDamage.isToggled() && mc.thePlayer.hurtTime != 0) {
            this.disable();
            return;
        }

        mc.thePlayer.setSprinting(false);
        mc.thePlayer.moveForward = 0;
        mc.thePlayer.moveStrafing = 0;
        mc.thePlayer.setSneaking(false);

        fakePlayer.rotationYaw = mc.thePlayer.rotationYaw;
        fakePlayer.rotationYawHead = mc.thePlayer.rotationYaw;
        fakePlayer.rotationPitch = mc.thePlayer.rotationPitch;

        double moveSpeed = 0.215 * speed.getInput();
        moveEntityWithKeys(fakePlayer, moveSpeed);

        if (lastChunkCoords[0] != fakePlayer.chunkCoordX || lastChunkCoords[1] != fakePlayer.chunkCoordZ) {
            int cx = fakePlayer.chunkCoordX;
            int cz = fakePlayer.chunkCoordZ;
            mc.theWorld.markBlockRangeForRenderUpdate(cx * 16, 0, cz * 16, cx * 16 + 15, 256, cz * 16 + 15);
        }

        lastChunkCoords[0] = fakePlayer.chunkCoordX;
        lastChunkCoords[1] = fakePlayer.chunkCoordZ;
    }

    private void moveEntityWithKeys(EntityOtherPlayerMP entity, double speed) {
        float yaw = entity.rotationYawHead;
        double radYaw = Math.toRadians(yaw);

        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            entity.posX += -Math.sin(radYaw) * speed;
            entity.posZ += Math.cos(radYaw) * speed;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindBack.getKeyCode())) {
            entity.posX -= -Math.sin(radYaw) * speed;
            entity.posZ -= Math.cos(radYaw) * speed;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindLeft.getKeyCode())) {
            double rad = Math.toRadians(yaw - 90);
            entity.posX += -Math.sin(rad) * speed;
            entity.posZ += Math.cos(rad) * speed;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindRight.getKeyCode())) {
            double rad = Math.toRadians(yaw + 90);
            entity.posX += -Math.sin(rad) * speed;
            entity.posZ += Math.cos(rad) * speed;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            entity.posY += 0.93 * speed;
        }
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode())) {
            entity.posY -= 0.93 * speed;
        }
    }

    @SubscribeEvent
    public void onRender(RenderWorldLastEvent event) {
        if (Utils.Player.isPlayerInGame()) {
            mc.thePlayer.prevRenderArmPitch = 700F;
            mc.thePlayer.renderArmPitch = 700F;
            Utils.HUD.drawBoxAroundEntity(mc.thePlayer, 1, 0.0, 0.0, Color.GREEN.getRGB(), false);
        }
    }

    @SubscribeEvent
    public void onMouse(MouseEvent event) {
        if (Utils.Player.isPlayerInGame() && event.button != -1) {
            event.setCanceled(true);
        }
    }
}
