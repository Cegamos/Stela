package keystrokesmod.client.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.component.ModuleButton;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.util.render.RoundedUtil;

public abstract class AbstractSlider extends Component {
	protected final ModuleButton module;
	protected int offset;

	private static final Color primaryColor = new Color(32, 34, 45, 230);
	private static final int nameColor = new Color(140, 144, 158).getRGB();
	private static final int valueColor = new Color(230, 232, 240).getRGB();

	public AbstractSlider(ModuleButton module, int offset) {
		this.module = module;
		this.offset = offset;
	}

	protected void drawSlider(int trackX, int trackY, int trackWidth, int fillX, int fillWidth, String nameLabel, String valueLabel) {
		RoundedUtil.drawRound(trackX, trackY, trackWidth, 2f, 1f, primaryColor);

		if (fillWidth > 0) {
			RoundedUtil.drawRound(fillX, trackY, fillWidth, 2f, 1f, Theme.getMainColor());
		}

		GL11.glPushMatrix();
		GL11.glScaled(0.5, 0.5, 0.5);

		final float yPos = (float) ((this.module.getCategory().getY() + this.offset + 3) * 2);

		mc.fontRendererObj.drawStringWithShadow(nameLabel, (float) (trackX * 2), yPos, nameColor);

		final int valWidth = mc.fontRendererObj.getStringWidth(valueLabel);
		mc.fontRendererObj.drawStringWithShadow(valueLabel, (float) ((trackX + trackWidth) * 2 - valWidth), yPos, valueColor);

		GL11.glPopMatrix();
	}

	@Override
	public void setComponentStartAt(int pos) {
		this.offset = pos;
	}

	@Override
	public int height() {
		return 0;
	}
}