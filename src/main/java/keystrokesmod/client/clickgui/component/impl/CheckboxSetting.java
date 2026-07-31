package keystrokesmod.client.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.component.ModuleButton;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.render.RoundedUtil;

public class CheckboxSetting extends Component {
    private final Mod mod;
    private final BooleanValue setting;
    private final ModuleButton module;
    private int offset;

    public CheckboxSetting(final Mod mod, final BooleanValue setting, final ModuleButton module, final int offset) {
        this.mod = mod;
        this.setting = setting;
        this.module = module;
        this.offset = offset;
    }

    @Override
    public void draw() {
        int boxX = this.module.getCategory().getX() + 8;
        int boxY = this.module.getCategory().getY() + this.offset + 4;
        boolean active = this.setting.getValue();

        Color mainColor = Theme.getMainColor();
        Color boxBg = active ? mainColor : new Color(28, 29, 38, 240);
        Color boxBorder = active ? mainColor : new Color(50, 52, 65, 255);

        RoundedUtil.drawRound(boxX - 0.5f, boxY - 0.5f, 7f, 7f, 1.5f, boxBorder);
        RoundedUtil.drawRound(boxX, boxY, 6f, 6f, 1f, boxBg);

        if (active) {
            RoundedUtil.drawRound(boxX + 2f, boxY + 2f, 2f, 2f, 0.5f, Color.WHITE);
        }

        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        mc.fontRendererObj.drawStringWithShadow(
            this.setting.getName(),
            (float) ((this.module.getCategory().getX() + 19) * 2),
            (float) ((this.module.getCategory().getY() + this.offset + 3) * 2),
            active ? new Color(230, 232, 240).getRGB() : new Color(135, 138, 150).getRGB()
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
