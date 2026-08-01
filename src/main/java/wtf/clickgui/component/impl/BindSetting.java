package wtf.clickgui.component.impl;

import java.awt.Color;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import wtf.clickgui.component.Component;
import wtf.clickgui.component.ModuleButton;
import wtf.clickgui.theme.Theme;
import wtf.module.modules.client.GuiModule;

public class BindSetting extends Component {
    private boolean isBinding;
    private final ModuleButton module;
    private int offset;

    public BindSetting(final ModuleButton module, final int offset) {
        this.module = module;
        this.offset = offset;
    }

    @Override
    public void draw() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);

        int labelX = this.module.getCategory().getX() + 8;
        int labelY = this.module.getCategory().getY() + this.offset + 3;

        mc.fontRendererObj.drawStringWithShadow(
            "Bind",
            (float) (labelX * 2),
            (float) (labelY * 2),
            new Color(140, 144, 158).getRGB()
        );

        String keyName = this.isBinding ? "Listening..." : Keyboard.getKeyName(this.module.mod.getKeycode());
        if (keyName == null || keyName.equalsIgnoreCase("NONE")) keyName = "NONE";

        int keyWidth = mc.fontRendererObj.getStringWidth(keyName);
        int rightX = (this.module.getCategory().getX() + this.module.getCategory().getWidth() - 8) * 2;

        Color bindColor = this.isBinding ? new Color(255, 90, 90) : Theme.getMainColor();

        mc.fontRendererObj.drawStringWithShadow(
            keyName,
            (float) (rightX - keyWidth),
            (float) (labelY * 2),
            bindColor.getRGB()
        );

        GL11.glPopMatrix();
    }

    @Override
    public void mouseDown(final int x, final int y, final int button) {
        if (!this.module.isVisible()) return;

        if (isHovered(x, y) && button == 0 && this.module.open) {
            this.isBinding = !this.isBinding;
        }
    }

    @Override
    public void keyTyped(final char typedChar, final int keyCode) {
        if (!this.module.mod.getName().equalsIgnoreCase("AutoConfig") && this.isBinding) {
            if (keyCode == 11 || keyCode == 1) { // Escape or Backspace
                if (this.module.mod instanceof GuiModule) {
                    this.module.mod.setbind(54);
                } else {
                    this.module.mod.setbind(0);
                }
            } else {
                this.module.mod.setbind(keyCode);
            }
            this.isBinding = false;
        }
    }

    @Override
    public void setComponentStartAt(final int pos) {
        this.offset = pos;
    }

    public boolean isHovered(final int mouseX, final int mouseY) {
        int x = this.module.getCategory().getX();
        int y = this.module.getCategory().getY() + this.offset;
        return mouseX > x && mouseX < x + this.module.getCategory().getWidth() && mouseY > y - 1 && mouseY < y + 12;
    }

    @Override
    public int height() {
        return 16;
    }
}
