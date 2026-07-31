package keystrokesmod.client.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.component.ModuleButton;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.value.impl.ModeValue;

public class ModeSetting extends Component {
	private final ModeValue mode;
	private final ModuleButton module;
	private int offset;

	public ModeSetting(final ModeValue mode, final ModuleButton module, final int offset) {
		this.mode = mode;
		this.module = module;
		this.offset = offset;
	}

	@Override
	public void draw() {
		int labelX = this.module.getCategory().getX() + 8;
		int labelY = this.module.getCategory().getY() + this.offset + 3;

		GL11.glPushMatrix();
		GL11.glScaled(0.5, 0.5, 0.5);

		mc.fontRendererObj.drawStringWithShadow(this.mode.getName(), (float) (labelX * 2), (float) (labelY * 2), new Color(140, 144, 158).getRGB());

		String modeText = String.valueOf(this.mode.getMode());
		int modeWidth = mc.fontRendererObj.getStringWidth(modeText);
		int rightX = (this.module.getCategory().getX() + this.module.getCategory().getWidth() - 8) * 2;

		mc.fontRendererObj.drawStringWithShadow(modeText, (float) (rightX - modeWidth), (float) (labelY * 2), Theme.getMainColor().getRGB());

		GL11.glPopMatrix();
	}

	@Override
	public void setComponentStartAt(final int pos) {
		this.offset = pos;
	}

	@Override
	public void mouseDown(int mouseX, int mouseY, int button) {
		if (!this.module.isVisible()) return;

		if (isHovered(mouseX, mouseY) && button == 0 && this.module.open) {
			this.mode.increment();
		} else if (isHovered(mouseX, mouseY) && button == 1 && this.module.open) {
			this.mode.decrement();
		}
	}

	@Override
	public boolean isVisible() {
		return this.mode.canDisplay();
	}

	private boolean isHovered(final int mouseX, final int mouseY) {
		int x = this.module.getCategory().getX();
		int y = this.module.getCategory().getY() + this.offset;
		return mouseX > x && mouseX < x + this.module.getCategory().getWidth() && mouseY > y && mouseY < y + 11;
	}
}
