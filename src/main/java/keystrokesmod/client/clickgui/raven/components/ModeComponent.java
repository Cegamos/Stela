package keystrokesmod.client.clickgui.raven.components;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.value.impl.ModeValue;
import net.minecraft.client.Minecraft;

public class ModeComponent extends Component {
    private final int c;
    private final ModeValue mode;
    private final ModuleComponent module;
    private int x;
    private int y;
    private int o;
    private boolean registeredClick;
    private boolean md;
    
    public ModeComponent(final ModeValue desc, final ModuleComponent b, final int o) {
        this.c = new Color(30, 144, 255).getRGB();
        this.registeredClick = false;
        this.md = false;
        this.mode = desc;
        this.module = b;
        this.x = b.category.getX() + b.category.getWidth();
        this.y = b.category.getY() + b.o;
        this.o = o;
    }
    
    @Override
    public void draw() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5, 0.5, 0.5);
        final int bruhWidth = (int)(mc.fontRendererObj.getStringWidth(this.mode.getName() + ": ") * 0.5);
        mc.fontRendererObj.drawString(this.mode.getName() + ": ", (float)((this.module.category.getX() + 4) * 2), (float)((this.module.category.getY() + this.o + 4) * 2), -1, true);
        mc.fontRendererObj.drawString(String.valueOf(this.mode.getMode()), (float)((this.module.category.getX() + 4 + bruhWidth) * 2), (float)((this.module.category.getY() + this.o + 4) * 2), Theme.getMainColor().getRGB(), true);
        GL11.glPopMatrix();
    }
    
    @Override
    public void update(final int mousePosX, final int mousePosY) {
        this.y = this.module.category.getY() + this.o;
        this.x = this.module.category.getX();
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
    public void mouseDown(int x, int y, int b) {
    	if (!this.module.isVisible()) return;

        if (i(x, y) && b == 0 && this.module.open) {
          this.mode.increment();
        } else if (i(x, y) && b == 1 && this.module.open) {
          this.mode.decrement();
        } 
      }
    
    @Override
    public void mouseReleased(final int x, final int y, final int m) {
    }
    
    @Override
    public void keyTyped(final char t, final int k) {
    }
    
    @Override
    public boolean isVisible() {
        return this.mode.canDisplay();
    }
    
    private boolean i(final int x, final int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 11;
    }
}
