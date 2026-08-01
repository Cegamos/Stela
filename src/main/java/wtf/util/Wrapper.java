package wtf.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.multiplayer.PlayerControllerMP;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.client.settings.KeyBinding;

public class Wrapper {

	protected static Minecraft mc = Minecraft.getMinecraft();
	
	public static Minecraft mc() {
		return Minecraft.getMinecraft();
	}
	
	protected static EntityPlayerSP getPlayer() {
		return mc.thePlayer;
	}
	
	protected static WorldClient getWorld() {
		return mc.theWorld;
	}
	
	protected static boolean ground() {
		return getPlayer().onGround;
	}
	
	protected static boolean air() {
		return !getPlayer().onGround;
	}
	
	protected static boolean sneaking() {
		return getPlayer().isSneaking();
	}
	
	protected static double motionX() {
		return getPlayer().motionX;
	}
	
	protected static double motionY() {
		return getPlayer().motionY;
	}
	
	protected static double motionZ() {
		return getPlayer().motionZ;
	}
	
	protected static double posX() {
		return getPlayer().posX;
	}
	
	protected static double posY() {
		return getPlayer().posY;
	}
	
	protected static double posZ() {
		return getPlayer().posZ;
	}
	
	protected static float yaw() {
		return getPlayer().rotationYaw;
	}
	
	protected static float pitch() {
		return getPlayer().rotationPitch;
	}
	
	protected static int hurtTime() {
		return getPlayer().hurtTime;
	}
	
	protected static GuiScreen currentScreen() {
		return mc.currentScreen;
	}
	
	protected static boolean checkScreen() {
		return currentScreen() != null;
	}
	
	protected static boolean checkGame() {
		return getPlayer() == null && getWorld() == null;
	}
	
	protected static PlayerControllerMP playerController() {
		return mc.playerController;
	}
	
	protected static GameSettings gameSetting() {
		return mc.gameSettings;
	}
	
	protected static void legitJump(boolean active) {
		KeyBinding.setKeyBindState(gameSetting().keyBindJump.getKeyCode(), active);
	}
}