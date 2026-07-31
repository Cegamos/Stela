package keystrokesmod.client.module.modules.movement;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;

@ModuleInfo(name = "InvMove", category = Category.Movement)
public class InvMove extends Mod {
	
	private final BooleanValue onlyClick = new BooleanValue("Only ClickGui", this, true);

	@Override
	public void update() {
		if (mc.currentScreen != null) {
			if (mc.currentScreen instanceof GuiChat) return;

			if (!onlyClick.isToggled() || mc.currentScreen instanceof ClickGui) {
				setMoveKey(mc.gameSettings.keyBindForward);
				setMoveKey(mc.gameSettings.keyBindBack);
				setMoveKey(mc.gameSettings.keyBindRight);
				setMoveKey(mc.gameSettings.keyBindLeft);
				setMoveKey(mc.gameSettings.keyBindJump);

				if (Keyboard.isKeyDown(208) && mc.thePlayer.rotationPitch < 90.0f) {
					mc.thePlayer.rotationPitch += 6.0f;
				}
				if (Keyboard.isKeyDown(200) && mc.thePlayer.rotationPitch > -90.0f) {
					mc.thePlayer.rotationPitch -= 6.0f;
				}
				if (Keyboard.isKeyDown(205)) {
					mc.thePlayer.rotationYaw += 6.0f;
				}
				if (Keyboard.isKeyDown(203)) {
					mc.thePlayer.rotationYaw -= 6.0f;
				}
			}
		}
	}

	private void setMoveKey(KeyBinding key) {
		KeyBinding.setKeyBindState(key.getKeyCode(), Keyboard.isKeyDown(key.getKeyCode()));
	}
}
