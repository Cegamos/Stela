package keystrokesmod.client.clickgui.raven.components;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import net.minecraft.client.Minecraft;

public class DescriptionComponent extends Component {
    private final int c;
    private final DescriptionValue desc;
    private final ModuleComponent p;
    private int o;
    
    public DescriptionComponent(final DescriptionValue desc, final ModuleComponent b, final int o) {
        this.c = new Color(226, 83, 47).getRGB();
        this.desc = desc;
        this.p = b;
        this.o = o;
    }
    
    @Override
    public void draw() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);

        String text = desc.getDesc();
        int textWidth = mc.fontRendererObj.getStringWidth(text);

        int centerX = (p.category.getX() + p.category.getWidth() / 2) * 2;
        int y = (p.category.getY() + o + 4) * 2;

        mc.fontRendererObj.drawString(
            text,
            centerX - textWidth / 2,
            y,
            this.c,
            true
        );

        GL11.glPopMatrix();
    }

    
    @Override
    public void update(final int mousePosX, final int mousePosY) {
    }
    
    @Override
    public void mouseDown(final int x, final int y, final int b) {
    	if (!this.p.isVisible()) return;
    }
    
    @Override
    public void mouseReleased(final int x, final int y, final int m) {
    }
    
    @Override
    public void keyTyped(final char t, final int k) {
    }
    
    @Override
    public void setComponentStartAt(final int n) {
        this.o = n;
    }
    
    @Override
    public int height() {
        return 0;
    }
    
    @Override
    public boolean isVisible() {
        return this.desc.canDisplay();
    }
}
