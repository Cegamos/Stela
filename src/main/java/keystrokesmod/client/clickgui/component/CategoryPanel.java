package keystrokesmod.client.clickgui.component;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class CategoryPanel {
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

    // Scrolling & Viewport Cache
    private int scrollOffset = 0;
    private int maxScroll = 0;
    private int lastDisplayWidth = -1;
    private int lastDisplayHeight = -1;
    private int cachedScaleFactor = 2;
    private int cachedScaledHeight = 240;

    public CategoryPanel(final Category category) {
        this.modulesInCategory = new ArrayList<Component>();
        this.n4m = false;
        this.pin = false;
        this.categoryName = category;
        this.width = 96;
        this.x = 5;
        this.y = 5;
        this.bh = 16;
        this.xx = 0;
        this.categoryOpened = false;
        this.inUse = false;

        int tY = this.bh + 3;
        for (final Mod mod : Raven.moduleManager.getModulesInCategory(this.categoryName)) {
            final ModuleButton b = new ModuleButton(mod, this, tY);
            this.modulesInCategory.add(b);
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

    public void scroll(int amount) {
        if (!this.categoryOpened) return;
        this.scrollOffset += amount;
        clampScroll();
    }

    private void clampScroll() {
        int totalModulesHeight = 0;
        final int size = this.modulesInCategory.size();
        for (int i = 0; i < size; i++) {
            totalModulesHeight += this.modulesInCategory.get(i).height();
        }

        int maxVisibleHeight = 260;
        if (totalModulesHeight > maxVisibleHeight) {
            this.maxScroll = totalModulesHeight - maxVisibleHeight;
        } else {
            this.maxScroll = 0;
        }

        if (this.scrollOffset < -this.maxScroll) {
            this.scrollOffset = -this.maxScroll;
        }
        if (this.scrollOffset > 0) {
            this.scrollOffset = 0;
        }
    }

    private void updateResolutionCache(Minecraft mc) {
        if (mc.displayWidth != lastDisplayWidth || mc.displayHeight != lastDisplayHeight) {
            ScaledResolution sr = new ScaledResolution(mc);
            cachedScaleFactor = sr.getScaleFactor();
            cachedScaledHeight = sr.getScaledHeight();
            lastDisplayWidth = mc.displayWidth;
            lastDisplayHeight = mc.displayHeight;
        }
    }

    public void rf(final FontRenderer renderer) {
        this.width = 96;

        int totalModulesHeight = 0;
        if (!this.modulesInCategory.isEmpty() && this.categoryOpened) {
            final int size = this.modulesInCategory.size();
            for (int i = 0; i < size; i++) {
                totalModulesHeight += this.modulesInCategory.get(i).height();
            }
        }

        int maxPanelHeight = Math.min(totalModulesHeight, 260);
        int totalPanelHeight = this.bh + (this.categoryOpened ? maxPanelHeight + 4 : 4);

        Color accentColor = Theme.getMainColor();
        Color bgDark = new Color(13, 14, 19, 238);
        Color headerDark = new Color(22, 24, 32, 250);
        Color outlineColor = new Color(45, 48, 65, 200);

        // Enterprise Shader Outline Card
        RoundedUtil.drawRoundOutline(this.x, this.y, this.width, totalPanelHeight, 6f, 1f, bgDark, outlineColor);

        // Header Card Background
        RoundedUtil.drawRound(this.x, this.y, this.width, this.bh + 2, 6f, headerDark);

        // Header Accent Underline
        RoundedUtil.drawRound(this.x + 2, this.y + this.bh + 1, this.width - 4, 1.5f, 0.75f, accentColor);

        // Category Name (Clean Enterprise Typography)
        String textToDraw = this.n4m ? this.pvp : this.categoryName.name();
        renderer.drawStringWithShadow(
            textToDraw,
            this.x + 8,
            this.y + 4,
            Color.WHITE.getRGB()
        );

        // Toggle Indicator (− / +)
        if (!this.n4m) {
            renderer.drawStringWithShadow(
                this.categoryOpened ? "−" : "+",
                this.x + this.width - 12,
                this.y + 4,
                accentColor.getRGB()
            );
        }

        // Render Modules inside GL Scissor Viewport when opened
        if (this.categoryOpened && !this.modulesInCategory.isEmpty()) {
            Minecraft mc = Minecraft.getMinecraft();
            updateResolutionCache(mc);

            int scissorX = this.x * cachedScaleFactor;
            int scissorY = (cachedScaledHeight - (this.y + this.bh + 3 + maxPanelHeight)) * cachedScaleFactor;
            int scissorWidth = this.width * cachedScaleFactor;
            int scissorHeight = (maxPanelHeight + 2) * cachedScaleFactor;

            GL11.glEnable(GL11.GL_SCISSOR_TEST);
            GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

            GL11.glPushMatrix();
            GL11.glTranslatef(0, this.scrollOffset, 0);

            final int size = this.modulesInCategory.size();
            for (int i = 0; i < size; i++) {
                this.modulesInCategory.get(i).draw();
            }

            GL11.glPopMatrix();
            GL11.glDisable(GL11.GL_SCISSOR_TEST);
        }
    }

    public void r3nd3r() {
        int o = this.bh + 3;
        final int size = this.modulesInCategory.size();
        for (int i = 0; i < size; i++) {
            final Component c = this.modulesInCategory.get(i);
            c.setComponentStartAt(o);
            o += c.height();
        }
        clampScroll();
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
        return x >= this.x + this.width - 15 && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }

    public boolean mousePressed(final int x, final int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }

    public boolean insideArea(final int x, final int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.bh;
    }

    public boolean insideAreaOrModules(final int x, final int y) {
        int totalHeight = this.bh;
        if (this.categoryOpened) {
            int modulesHeight = 0;
            final int size = this.modulesInCategory.size();
            for (int i = 0; i < size; i++) {
                modulesHeight += this.modulesInCategory.get(i).height();
            }
            totalHeight += Math.min(modulesHeight, 260) + 4;
        }
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + totalHeight;
    }

    public String getName() {
        return String.valueOf(this.modulesInCategory);
    }

    public void setLocation(final int parseInt, final int parseInt1) {
        this.x = parseInt;
        this.y = parseInt1;
    }

    public int getScrollOffset() {
        return scrollOffset;
    }
}