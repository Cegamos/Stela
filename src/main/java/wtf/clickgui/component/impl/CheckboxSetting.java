package wtf.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import wtf.clickgui.component.Component;
import wtf.clickgui.component.ModuleButton;
import wtf.clickgui.theme.Theme;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.util.render.ColorUtil;
import wtf.util.render.RoundedUtil;
import wtf.util.render.animation.Animation;
import wtf.util.render.animation.Direction;
import wtf.util.render.animation.SmoothStepAnimation;

public class CheckboxSetting extends Component {
    private final Mod mod;
    private final BooleanValue setting;
    private final ModuleButton module;
    private int offset;
    private final Animation toggleAnimation = new SmoothStepAnimation(140, 1.0);

    public CheckboxSetting(final Mod mod, final BooleanValue setting, final ModuleButton module, final int offset) {
        this.mod = mod;
        this.setting = setting;
        this.module = module;
        this.offset = offset;
    }

    @Override
    public void draw() {
        int trackX = this.module.getCategory().getX() + 8;
        float trackY = this.module.getCategory().getY() + this.offset + 3.5f;
        boolean active = this.setting.getValue();

        toggleAnimation.setDirection(active ? Direction.FORWARDS : Direction.BACKWARDS);
        double animProgress = toggleAnimation.getOutput();

        Color mainColor = Theme.getMainColor();
        Color inactiveBg = new Color(30, 32, 42, 240);
        Color trackBg = ColorUtil.interpolateColorC(inactiveBg, mainColor, (float) animProgress);

        Color inactiveKnob = new Color(130, 135, 150);
        Color knobColor = ColorUtil.interpolateColorC(inactiveKnob, Color.BLACK, (float) animProgress);

        RoundedUtil.drawRound(trackX, trackY, 12f, 5f, 2.5f, trackBg);

        float knobX = (float) (trackX + 0.75f + (animProgress * 7.0f));
        RoundedUtil.drawRound(knobX, trackY + 0.75f, 3.5f, 3.5f, 1.75f, knobColor);

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);

        int startX = (this.module.getCategory().getX() + 23) * 2;
        int maxX = (this.module.getCategory().getX() + this.module.getCategory().getWidth() - 6) * 2;
        int maxNameWidth = maxX - startX;

        String displayName = this.setting.getName();
        if (maxNameWidth > 10 && mc.fontRendererObj.getStringWidth(displayName) > maxNameWidth) {
            displayName = mc.fontRendererObj.trimStringToWidth(displayName, maxNameWidth - mc.fontRendererObj.getStringWidth("...")) + "...";
        }

        Color textColor = ColorUtil.interpolateColorC(new Color(135, 138, 150), new Color(230, 232, 240), (float) animProgress);
        mc.fontRendererObj.drawStringWithShadow(
            displayName,
            (float) startX,
            (float) ((this.module.getCategory().getY() + this.offset + 6) * 2),
            textColor.getRGB()
        );
        GL11.glPopMatrix();
    }

    @Override
    public void setComponentStartAt(final int pos) {
        this.offset = pos;
    }

    @Override
    public void mouseDown(final int x, final int y, final int button) {
        if (!this.module.isVisible()) return;
        if (isHovered(x, y) && button == 0 && this.module.open) {
            this.setting.toggle();
            this.mod.guiButtonToggled(this.setting);
        }
    }

    @Override
    public boolean isVisible() {
        return this.setting.canDisplay();
    }

    public boolean isHovered(final int mouseX, final int mouseY) {
        int x = this.module.getCategory().getX();
        int y = this.module.getCategory().getY() + this.offset;
        return mouseX > x && mouseX < x + this.module.getCategory().getWidth() && mouseY > y && mouseY < y + 12;
    }
}
