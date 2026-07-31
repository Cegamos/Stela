package keystrokesmod.client.clickgui.raven.components;

import java.awt.Color;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;

public class TickComponent extends Component {
    private final int c;
    private final int boxC;
    private final Mod mod;
    private final BooleanValue cl1ckbUtt0n;
    private final ModuleComponent module;
    private int o;
    private int x;
    private int y;
    private int boxSize;
    
    public TickComponent(final Mod mod, final BooleanValue op, final ModuleComponent b, final int o) {
        this.c = new Color(20, 255, 0).getRGB();
        this.boxC = new Color(169, 169, 169).getRGB();
        this.boxSize = 6;
        this.mod = mod;
        this.cl1ckbUtt0n = op;
        this.module = b;
        this.x = b.category.getX() + b.category.getWidth();
        this.y = b.category.getY() + b.o;
        this.o = o;
    }

    @Override
    public void draw() {
        GL11.glPushMatrix();
        GL11.glScaled(0.5D, 0.5D, 0.5D);
        mc.fontRendererObj.drawStringWithShadow(this.cl1ckbUtt0n.getValue() ? "[+]  " + this.cl1ckbUtt0n.getName() : "[-]  " + this.cl1ckbUtt0n.getName(), (float)((this.module.category.getX() + 4) * 2), (float)((this.module.category.getY() + this.o + 5) * 2), this.cl1ckbUtt0n.getValue() ? Theme.getMainColor().getRGB() : -1);
        GL11.glPopMatrix();
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
    public void update(final int mousePosX, final int mousePosY) {
        this.y = this.module.category.getY() + this.o;
        this.x = this.module.category.getX();
    }
    
    @Override
    public void mouseDown(final int x, final int y, final int b) {
    	if (!this.module.isVisible()) return;
        if (this.i(x, y) && b == 0 && this.module.open) {
            this.cl1ckbUtt0n.toggle();
            this.mod.guiButtonToggled(this.cl1ckbUtt0n);
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
        return this.cl1ckbUtt0n.canDisplay();
    }
    
    public boolean i(final int x, final int y) {
        return x > this.x && x < this.x + this.module.category.getWidth() && y > this.y && y < this.y + 11;
    }
}
