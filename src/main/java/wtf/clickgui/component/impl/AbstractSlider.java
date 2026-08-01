package wtf.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import wtf.clickgui.component.Component;
import wtf.clickgui.component.ModuleButton;
import wtf.clickgui.theme.Theme;
import wtf.util.render.RoundedUtil;
import wtf.util.render.animation.ContinualAnimation;

public abstract class AbstractSlider extends Component {
	protected final ModuleButton module;
	protected int offset;
	private final ContinualAnimation fillAnimation = new ContinualAnimation();

	private static final Color primaryColor = new Color(32, 34, 45, 230);
	private static final int nameColor = new Color(140, 144, 158).getRGB();
	private static final int valueColor = new Color(230, 232, 240).getRGB();

	public AbstractSlider(ModuleButton module, int offset) {
		this.module = module;
		this.offset = offset;
	}

	protected void drawSlider(int trackX, int trackY, int trackWidth, int fillX, int fillWidth, String nameLabel, String valueLabel) {
		RoundedUtil.drawRound(trackX, trackY, trackWidth, 2f, 1f, primaryColor);

		fillAnimation.animate((float) fillWidth, 100);
		float animatedFill = fillAnimation.getOutput();

		if (animatedFill > 0) {
			RoundedUtil.drawRound(fillX, trackY, animatedFill, 2f, 1f, Theme.getMainColor());
		}

		GL11.glPushMatrix();
		GL11.glScaled(0.5, 0.5, 0.5);

		final float yPos = (float) ((this.module.getCategory().getY() + this.offset + 3) * 2);

		final int valWidth = mc.fontRendererObj.getStringWidth(valueLabel);
		final int startX = trackX * 2;
		final int endX = (trackX + trackWidth) * 2;
		final int maxNameWidth = endX - valWidth - startX - 6;

		String displayName = nameLabel;
		if (maxNameWidth > 10 && mc.fontRendererObj.getStringWidth(displayName) > maxNameWidth) {
			displayName = mc.fontRendererObj.trimStringToWidth(displayName, maxNameWidth - mc.fontRendererObj.getStringWidth("...")) + "...";
		}

		mc.fontRendererObj.drawStringWithShadow(displayName, (float) startX, yPos, nameColor);
		mc.fontRendererObj.drawStringWithShadow(valueLabel, (float) (endX - valWidth), yPos, valueColor);

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