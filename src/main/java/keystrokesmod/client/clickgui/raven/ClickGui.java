package keystrokesmod.client.clickgui.raven;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.concurrent.ScheduledFuture;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.raven.components.CategoryComponent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.gui.GuiScreen;

public class ClickGui extends GuiScreen {
    private ScheduledFuture<?> sf;
    private final ArrayList<CategoryComponent> categoryList;
    public final Terminal terminal;
    private int guiYMoveLeft = 0;
    private static final int SCROLL_SPEED = 30;
    
    public ClickGui() {
        this.terminal = new Terminal();
        this.categoryList = new ArrayList<CategoryComponent>();
        int topOffset = 5;
        Category[] values;
        for (int categoryAmount = (values = Category.values()).length, category = 0; category < categoryAmount; ++category) {
            final Category moduleCategory = values[category];
            final CategoryComponent currentModuleCategory = new CategoryComponent(moduleCategory);
            currentModuleCategory.setY(topOffset);
            this.categoryList.add(currentModuleCategory);
            topOffset += 20;
        }
        this.terminal.setLocation(5, topOffset);
        this.terminal.setSize(138, 103);
    }

    @Override
    public void initGui() {
        super.initGui();
    }
    
    @Override
    public void drawScreen(final int x, final int y, final float p) {
        drawRect(0, 0, this.width, this.height, -1308622848);
        
        if (guiYMoveLeft != 0) {
            int step = (int) (guiYMoveLeft * 0.15);
            if (step == 0) {
                guiYMoveLeft = 0;
            } else {
                for (CategoryComponent category : this.categoryList) {
                    category.setY(category.getY() + step);
                }
                guiYMoveLeft -= step;
            }
        }

        this.mc.fontRendererObj.drawString("Raven B+ v" + Raven.VERSION + " | Config: " + keystrokesmod.client.config.ConfigManager.getCurrentProfileName(), 4, this.height - 3 - this.mc.fontRendererObj.FONT_HEIGHT, Utils.Client.astolfoColorsDraw(10, 14, 3000f));

        for (final CategoryComponent category : this.categoryList) {
            category.rf(this.fontRendererObj);
            category.up(x, y);
            for (final Component module : category.getModules()) {
                module.update(x, y);
            }
        }

        this.terminal.update(x, y);
        this.terminal.draw();
    }
    
    @Override
    public void mouseClicked(final int x, final int y, final int mouseButton) throws IOException {
        final Iterator<CategoryComponent> btnCat = this.categoryList.iterator();
        this.terminal.mouseDown(x, y, mouseButton);
        if (this.terminal.overPosition(x, y)) {
            return;
        }
        while (btnCat.hasNext()) {
            final CategoryComponent category = btnCat.next();
            if (category.insideArea(x, y) && !category.i(x, y) && !category.mousePressed(x, y) && mouseButton == 0) {
                category.mousePressed(true);
                category.xx = x - category.getX();
                category.yy = y - category.getY();
            }
            if (category.mousePressed(x, y) && mouseButton == 0) {
                category.setOpened(!category.isOpened());
            }
            if (category.i(x, y) && mouseButton == 0) {
                category.cv(!category.p());
            }
            if (category.isOpened() && !category.getModules().isEmpty()) {
                for (final Component c : category.getModules()) {
                    c.mouseDown(x, y, mouseButton);
                }
            }
        }
    }
    
    @Override
    public void mouseReleased(final int x, final int y, final int s) {
        this.terminal.mouseReleased(x, y, s);
        if (this.terminal.overPosition(x, y)) {
            return;
        }
        if (s == 0) {
            for (final CategoryComponent c4t : this.categoryList) {
                c4t.mousePressed(false);
            }
            for (final CategoryComponent c4t : this.categoryList) {
                if (c4t.isOpened() && !c4t.getModules().isEmpty()) {
                    for (final Component c : c4t.getModules()) {
                        c.mouseReleased(x, y, s);
                    }
                }
            }
            return;
        }
        if (Raven.clientConfig != null) {
            Raven.clientConfig.saveConfig();
        }
    }
    
    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
    	this.terminal.keyTyped(typedChar, keyCode);
        switch (keyCode) {
        case Keyboard.KEY_UP: guiYMoveLeft += SCROLL_SPEED;
        case Keyboard.KEY_DOWN: guiYMoveLeft -= SCROLL_SPEED;
        case Keyboard.KEY_ESCAPE: mc.displayGuiScreen(null);
        }
        
        for (final CategoryComponent cat : this.categoryList) {
            if (cat.isOpened() && !cat.getModules().isEmpty()) {
                for (final Component c : cat.getModules()) {
                    c.keyTyped(typedChar, keyCode);
                }
            }
        }
        
        super.keyTyped(typedChar, keyCode);
    }
    
    @Override
    public void onGuiClosed() {
        if (this.sf != null) {
            this.sf.cancel(true);
            this.sf = null;
        }
        keystrokesmod.client.config.ConfigManager.saveConfigByName(keystrokesmod.client.config.ConfigManager.getCurrentProfileName());
        Raven.clientConfig.saveConfig();
    }
    
    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int dWheel = Mouse.getDWheel();
        if (dWheel != 0) mouseScrolled(dWheel);
    }

    private void mouseScrolled(int dWheel) {
        guiYMoveLeft += (dWheel > 0 ? SCROLL_SPEED : -SCROLL_SPEED);
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public ArrayList<CategoryComponent> getCategoryList() {
        return this.categoryList;
    }
}
