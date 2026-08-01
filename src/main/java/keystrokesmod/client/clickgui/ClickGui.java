package keystrokesmod.client.clickgui;

import java.io.IOException;
import java.util.ArrayList;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.clickgui.component.CategoryPanel;
import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.window.ManagementWindow;
import keystrokesmod.client.clickgui.window.TerminalWindow;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ResourceLocation;

public class ClickGui extends GuiScreen {
    
    private static final ResourceLocation blur = new ResourceLocation("shaders/post/blur.json");
    private final ArrayList<CategoryPanel> categoryList;
    public final TerminalWindow terminal;
    public final ManagementWindow managementWindow;
    
    private String cachedWatermark;

    public ClickGui() {
        this.terminal = new TerminalWindow();
        this.managementWindow = new ManagementWindow();
        this.categoryList = new ArrayList<>(Category.values().length);
        
        int leftOffset = 10;
        for (final Category moduleCategory : Category.values()) {
            final CategoryPanel currentModuleCategory = new CategoryPanel(moduleCategory);
            currentModuleCategory.setX(leftOffset);
            currentModuleCategory.setY(10);
            this.categoryList.add(currentModuleCategory);
            leftOffset += 105;
        }
        
        this.terminal.setLocation(10, 280);
        this.terminal.setSize(138, 103);
    }

    @Override
    public void initGui() {
        super.initGui();
        this.cachedWatermark = Kevin.NAME + " v" + Kevin.VERSION + " | Config: " + ConfigManager.getCurrentProfileName();
        
        try {
            if (this.mc != null && this.mc.theWorld != null) {
                this.mc.entityRenderer.loadShader(blur);
            }
        } catch (Exception ignored) {}
    }

    @Override
    public void drawScreen(final int x, final int y, final float p) {
        drawRect(0, 0, this.width, this.height, 0x900B0B0E);

        this.mc.fontRendererObj.drawStringWithShadow(
            this.cachedWatermark,
            6,
            this.height - 4 - this.mc.fontRendererObj.FONT_HEIGHT,
            Utils.Client.astolfoColorsDraw(10, 14, 3000f)
        );

        for (final CategoryPanel category : this.categoryList) {
            category.rf(this.fontRendererObj);
            category.up(x, y);

            int adjustedMouseY = y - category.getScrollOffset();
            for (final Component module : category.getModules()) {
                module.update(x, adjustedMouseY);
            }
        }

        this.terminal.update(x, y);
        this.terminal.draw();

        this.managementWindow.update(x, y);
        this.managementWindow.draw();
    }

    @Override
    public void mouseClicked(final int x, final int y, final int mouseButton) throws IOException {
        this.terminal.mouseDown(x, y, mouseButton);
        if (this.terminal.overPosition(x, y)) {
            return;
        }

        this.managementWindow.mouseDown(x, y, mouseButton);
        if (this.managementWindow.overPosition(x, y)) {
            return;
        }

        for (final CategoryPanel category : this.categoryList) {
            if (category.insideArea(x, y) && mouseButton == 0) {
                category.mousePressed(true);
                category.xx = x - category.getX();
                category.yy = y - category.getY();
                break;
            }

            if (category.mousePressed(x, y) && mouseButton == 1) {
                category.setOpened(!category.isOpened());
                break;
            }

            if (category.isOpened() && !category.getModules().isEmpty()) {
                int adjustedMouseY = y - category.getScrollOffset();
                if (x >= category.getX() && x <= category.getX() + category.getWidth()) {
                    for (final Component c : category.getModules()) {
                        c.mouseDown(x, adjustedMouseY, mouseButton);
                    }
                }
            }
        }
    }

    @Override
    public void mouseReleased(final int x, final int y, final int s) {
        this.terminal.mouseReleased(x, y, s);
        this.managementWindow.mouseReleased(x, y, s);
        if (this.terminal.overPosition(x, y) || this.managementWindow.overPosition(x, y)) {
            return;
        }

        if (s == 0) {
            for (final CategoryPanel category : this.categoryList) {
                category.mousePressed(false);
                if (category.isOpened() && !category.getModules().isEmpty()) {
                    int adjustedMouseY = y - category.getScrollOffset();
                    for (final Component c : category.getModules()) {
                        c.mouseReleased(x, adjustedMouseY, s);
                    }
                }
            }
        }
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        this.terminal.keyTyped(typedChar, keyCode);
        if (keyCode == Keyboard.KEY_ESCAPE) {
            mc.displayGuiScreen(null);
            return;
        }

        for (final CategoryPanel cat : this.categoryList) {
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
        try {
            if (this.mc != null && this.mc.entityRenderer != null && this.mc.entityRenderer.isShaderActive()) {
                this.mc.entityRenderer.stopUseShader();
            }
        } catch (Exception ignored) {}

        ConfigManager.saveConfigByName(ConfigManager.getCurrentProfileName());
        if (Kevin.clientConfig != null) {
            Kevin.clientConfig.saveConfig();
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        final int dWheel = Mouse.getDWheel();
        
        if (dWheel != 0) {
            final int mouseX = (Mouse.getEventX() * this.width) / this.mc.displayWidth;
            final int mouseY = this.height - (Mouse.getEventY() * this.height) / this.mc.displayHeight - 1;
            
            if (this.terminal.overPosition(mouseX, mouseY)) {
                this.terminal.handleMouseInput(dWheel);
                return;
            }
            if (this.managementWindow.overPosition(mouseX, mouseY)) {
                this.managementWindow.handleMouseInput(dWheel);
                return;
            }

            final int scrollAmount = dWheel > 0 ? 18 : -18;
            boolean handled = false;

            for (final CategoryPanel category : this.categoryList) {
                if (category.insideAreaOrModules(mouseX, mouseY)) {
                    category.scroll(scrollAmount);
                    handled = true;
                    break;
                }
            }

            if (!handled) {
                for (final CategoryPanel category : this.categoryList) {
                    category.scroll(scrollAmount);
                }
            }
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    public ArrayList<CategoryPanel> getCategoryList() {
        return this.categoryList;
    }
}