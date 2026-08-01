package wtf.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import wtf.clickgui.component.Component;
import wtf.clickgui.component.ModuleButton;
import wtf.module.value.impl.DescriptionValue;

public class TextSetting extends Component {
	private final DescriptionValue textValue;
	private final ModuleButton module;
	private int offset;

	public TextSetting(final DescriptionValue textValue, final ModuleButton module, final int offset) {
		this.textValue = textValue;
		this.module = module;
		this.offset = offset;
	}

	@Override
	public void draw() {
		GL11.glPushMatrix();
		GL11.glScaled(0.5, 0.5, 0.5);

		int labelX = this.module.getCategory().getX() + 8;
		int labelY = this.module.getCategory().getY() + this.offset + 3;

		mc.fontRendererObj.drawStringWithShadow(this.textValue.getName(), (float) (labelX * 2), (float) (labelY * 2), new Color(150, 155, 170).getRGB());
		GL11.glPopMatrix();
	}

	@Override
	public void setComponentStartAt(final int pos) {
		this.offset = pos;
	}

	@Override
	public boolean isVisible() {
		return this.textValue.canDisplay();
	}
}
