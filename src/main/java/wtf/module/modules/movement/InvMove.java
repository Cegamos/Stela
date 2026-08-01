package wtf.module.modules.movement;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.settings.KeyBinding;
import wtf.clickgui.ClickGui;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;

@ModuleInfo(name = "InvMove", category = Category.Movement)
public class InvMove extends Mod {
	
	private final BooleanValue onlyClick = new BooleanValue("Only ClickGui", this, true);

	@Override
	public void update() {
		if (mc.currentScreen != null) {
			if (mc.currentScreen instanceof GuiChat) return;

			if (!onlyClick.getValue() || mc.currentScreen instanceof ClickGui) {
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
