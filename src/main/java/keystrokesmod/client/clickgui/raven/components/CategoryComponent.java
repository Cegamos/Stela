package keystrokesmod.client.clickgui.raven.components;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.modules.client.GuiModule;
import keystrokesmod.client.util.render.RenderUtil;
import net.minecraft.client.gui.FontRenderer;

public class CategoryComponent
{
    public ArrayList<Component> modulesInCategory;
    public Category categoryName;
    private boolean categoryOpened;
    private int width;
    private int y;
    private int x;
    private final int bh;
    public boolean inUse;
    public int xx;
    public int yy;
    public boolean n4m;
    public String pvp;
    public boolean pin;
    private int chromaSpeed;
    private double marginY;
    private double marginX;
    
    public CategoryComponent(final Category category) {
        this.modulesInCategory = new ArrayList<Component>();
        this.n4m = false;
        this.pin = false;
        this.categoryName = category;
        this.width = 92;
        this.x = 5;
        this.y = 5;
        this.bh = 13;
        this.xx = 0;
        this.categoryOpened = false;
        this.inUse = false;
        this.chromaSpeed = 3;
        int tY = this.bh + 3;
        this.marginX = 80.0;
        this.marginY = 4.5;
        for (final Mod mod : Raven.moduleManager.getModulesInCategory(this.categoryName)) {
            final ModuleComponent b = new ModuleComponent(mod, this, tY);
            this.modulesInCategory.add((Component)b);
            tY += 16;
        }
    }
    
    public ArrayList<Component> getModules() {
        return this.modulesInCategory;
    }
    
    public void setX(final int n) {
        this.x = n;
        if (Raven.clientConfig != null) {
            Raven.clientConfig.saveConfig();
        }
    }
    
    public void setY(final int y) {
        this.y = y;
        if (Raven.clientConfig != null) {
            Raven.clientConfig.saveConfig();
        }
    }
    
    public void mousePressed(final boolean d) {
        this.inUse = d;
    }
    
    public boolean p() {
        return this.pin;
    }
    
    public void cv(final boolean on) {
        this.pin = on;
    }
    
    public boolean isOpened() {
        return this.categoryOpened;
    }
    
    public void setOpened(final boolean on) {
        this.categoryOpened = on;
        if (Raven.clientConfig != null) {
            Raven.clientConfig.saveConfig();
        }
    }
    
    public void rf(final FontRenderer renderer) {
    	GuiModule gui = (GuiModule) Raven.moduleManager.getModuleByClazz(GuiModule.class);

        this.width = 92;
        if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
            int categoryHeight = 0;
            for (final Component moduleRenderManager : this.modulesInCategory) {
                categoryHeight += moduleRenderManager.height();
            }
            RenderUtil.drawBorderedRoundedRect1(this.x - 1, this.y, this.x + this.width + 1, this.y + this.bh + categoryHeight + 4, 10, 2, Theme.getMainColor().getRGB(), Theme.getBackColor().getRGB());
        } else {
            RenderUtil.drawBorderedRoundedRect1(this.x - 1, this.y, this.x + this.width + 1, this.y + this.bh + 4, 10, 2, Theme.getMainColor().getRGB(), Theme.getBackColor().getRGB());

        }
        String textToDraw = this.n4m ? this.pvp : this.categoryName.name();

        int textWidth = renderer.getStringWidth(textToDraw);
        float centeredX = this.x + (this.width - textWidth) / 2.0f;

        renderer.drawString(
            textToDraw,
            centeredX,
            this.y + 4,
            Theme.getMainColor().getRGB(),
            false
        );
        
        if (!this.n4m) {
            GL11.glPushMatrix();
            renderer.drawString(this.categoryOpened ? "-" : "+", (float)(this.x + this.marginX), (float)(this.y + this.marginY), Color.white.getRGB(), false);
            GL11.glPopMatrix();
            if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {
                for (final Component c2 : this.modulesInCategory) {
                    c2.draw();
                }
            }
        }
    }
    
    public void r3nd3r() {
        int o = this.bh + 3;
        for (final Component c : this.modulesInCategory) {
            c.setComponentStartAt(o);
            o += c.height();
        }
    }
    
    public int getX() {
        return this.x;
    }
    
    public int getY() {
        return this.y;
    }
    
    public int getWidth() {
        return this.width;
    }
    
    public void up(final int x, final int y) {
        if (this.inUse) {
            this.setX(x - this.xx);
            this.setY(y - this.yy);
        }
    }
    
    public boolean i(final int x, final int y) {
        return x >= this.x + 92 - 13 && x <= this.x + this.width && y >= this.y + 2.0f && y <= this.y + this.bh + 1;
    }
    
    public boolean mousePressed(final int x, final int y) {
        return x >= this.x + 77 && x <= this.x + this.width - 6 && y >= this.y + 2.0f && y <= this.y + this.bh + 1;
    }
    
    public boolean insideArea(final int x, final int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }
    
    public String getName() {
        return String.valueOf(this.modulesInCategory);
    }
    
    public void setLocation(final int parseInt, final int parseInt1) {
        this.x = parseInt;
        this.y = parseInt1;
    }
}
