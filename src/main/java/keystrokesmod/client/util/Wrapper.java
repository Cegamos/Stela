package keystrokesmod.client.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class Wrapper {

	protected static Minecraft mc = Minecraft.getMinecraft();
	
	protected EntityPlayerSP getPlayer() {
		return mc.thePlayer;
	}
	
	protected WorldClient getWorld() {
		return mc.theWorld;
	}
	
	protected boolean ground() {
		return getPlayer().onGround;
	}
	
	protected boolean air() {
		return !getPlayer().onGround;
	}
	
	protected boolean sneaking() {
		return getPlayer().isSneaking();
	}
	
	protected double motionX() {
		return getPlayer().motionX;
	}
	
	protected double motionY() {
		return getPlayer().motionY;
	}
	
	protected double motionZ() {
		return getPlayer().motionZ;
	}
	
	protected double posX() {
		return getPlayer().posX;
	}
	
	protected double posY() {
		return getPlayer().posY;
	}
	
	protected double posZ() {
		return getPlayer().posZ;
	}
	
	protected float yaw() {
		return getPlayer().rotationYaw;
	}
	
	protected float pitch() {
		return getPlayer().rotationPitch;
	}
	
	protected int hurtTime() {
		return getPlayer().hurtTime;
	}
	
	protected GuiScreen currentScreen() {
		return mc.currentScreen;
	}
	
	protected boolean checkScreen() {
		return currentScreen() != null;
	}
	
	protected boolean checkGame() {
		return getPlayer() == null && getWorld() == null;
	}
	
	protected PlayerControllerMP playerController() {
		return mc.playerController;
	}
	
	protected GameSettings gameSetting() {
		return mc.gameSettings;
	}
	
	protected void legitJump(boolean active) {
		KeyBinding.setKeyBindState(gameSetting().keyBindJump.getKeyCode(), active);
	}
}
