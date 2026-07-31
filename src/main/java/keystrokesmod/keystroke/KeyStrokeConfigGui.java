package keystrokesmod.keystroke;

import java.io.IOException;

import keystrokesmod.client.config.ClientConfig;
import keystrokesmod.client.util.input.MouseManager;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiScreen;

public class KeyStrokeConfigGui extends GuiScreen {
    private static final String[] colors = new String[] { "White", "Red", "Green", "Blue", "Yellow", "Purple", "Rainbow" };
    private GuiButton modeBtn;
    private GuiButton textColorBtn;
    private GuiButton showMouseBtn;
    private GuiButton outlineBtn;
    private boolean d;
    private int lx;
    private int ly;
    
    public KeyStrokeConfigGui() {
        this.d = false;
    }

    @Override
    public void initGui() {
        this.buttonList.add(this.modeBtn = new GuiButton(0, this.width / 2 - 70, this.height / 2 - 28, 140, 20, "Mod: " + (KeyStroke.enabled ? "Enabled" : "Disabled")));
        this.buttonList.add(this.textColorBtn = new GuiButton(1, this.width / 2 - 70, this.height / 2 - 6, 140, 20, "Text color: " + KeyStrokeConfigGui.colors[KeyStroke.currentColorNumber]));
        this.buttonList.add(this.showMouseBtn = new GuiButton(2, this.width / 2 - 70, this.height / 2 + 16, 140, 20, "Show mouse buttons: " + (KeyStroke.showMouseButtons ? "On" : "Off")));
        this.buttonList.add(this.outlineBtn = new GuiButton(3, this.width / 2 - 70, this.height / 2 + 38, 140, 20, "Outline: " + (KeyStroke.outline ? "On" : "Off")));
    }
    
    @Override
    public void drawScreen(final int mouseX, final int mouseY, final float partialTicks) {
        KeyStrokeMod.getKeyStrokeRenderer().renderKeystrokes();
        super.drawScreen(mouseX, mouseY, partialTicks);
    }
    
    @Override
    protected void actionPerformed(final GuiButton button) {
        final KeyStroke st = KeyStrokeMod.getKeyStroke();
        if (button == this.modeBtn) {
            KeyStroke.enabled = !KeyStroke.enabled;
            this.modeBtn.displayString = "Mod: " + (KeyStroke.enabled ? "Enabled" : "Disabled");
        }
        else if (button == this.textColorBtn) {
            KeyStroke.currentColorNumber = ((KeyStroke.currentColorNumber == 6) ? 0 : (KeyStroke.currentColorNumber + 1));
            this.textColorBtn.displayString = "Text color: " + KeyStrokeConfigGui.colors[KeyStroke.currentColorNumber];
        }
        else if (button == this.showMouseBtn) {
            KeyStroke.showMouseButtons = !KeyStroke.showMouseButtons;
            this.showMouseBtn.displayString = "Show mouse buttons: " + (KeyStroke.showMouseButtons ? "On" : "Off");
        }
        else if (button == this.outlineBtn) {
            KeyStroke.outline = !KeyStroke.outline;
            this.outlineBtn.displayString = "Outline: " + (KeyStroke.outline ? "On" : "Off");
        }
    }
    
    @Override
    protected void mouseClicked(final int mouseX, final int mouseY, final int button) {
        try {
            super.mouseClicked(mouseX, mouseY, button);
        }
        catch (IOException ex) {}
        if (button == 0) {
            MouseManager.addLeftClick();
            final KeyStroke st = KeyStrokeMod.getKeyStroke();
            final int startX = KeyStroke.x;
            final int startY = KeyStroke.y;
            final int endX = startX + 74;
            final int endY = startY + (KeyStroke.showMouseButtons ? 74 : 50);
            if (mouseX >= startX && mouseX <= endX && mouseY >= startY && mouseY <= endY) {
                this.d = true;
                this.lx = mouseX;
                this.ly = mouseY;
            }
        }
        else if (button == 1) {
            MouseManager.addRightClick();
        }
    }
    
    @Override
    protected void mouseReleased(final int mouseX, final int mouseY, final int action) {
        super.mouseReleased(mouseX, mouseY, action);
        this.d = false;
    }
    
    @Override
    protected void mouseClickMove(final int mouseX, final int mouseY, final int lastButtonClicked, final long timeSinceMouseClick) {
        super.mouseClickMove(mouseX, mouseY, lastButtonClicked, timeSinceMouseClick);
        if (this.d) {
            KeyStroke.x = KeyStroke.x + mouseX - this.lx;
            KeyStroke.y = KeyStroke.y + mouseY - this.ly;
            this.lx = mouseX;
            this.ly = mouseY;
        }
    }
    
    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
    
    @Override
    public void onGuiClosed() {
        ClientConfig.saveKeyStrokeSettingsToConfigFile();
    }
}
