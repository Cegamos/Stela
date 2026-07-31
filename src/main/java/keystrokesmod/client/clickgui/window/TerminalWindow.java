package keystrokesmod.client.clickgui.window;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.command.CommandManager;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.chat.ChatUtil;
import keystrokesmod.client.util.render.RoundedUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;

public class TerminalWindow extends Component {
    private int x;
    private int y;
    private int width;
    private int height;
    private final int barHeight;
    private final int minWidth;
    private final int minHeight;
    private final int resizeHandleSize;

    public boolean opened;
    public boolean hidden;
    private boolean resizing;
    private boolean dragging;
    private boolean focused;

    private String inputText;
    
    private static final List<TerminalLine> out = new ArrayList<>();
    private static final List<String> commandHistory = new ArrayList<>();
    private int historyIndex = -1;

    private double windowStartDragX;
    private double windowStartDragY;
    private double mouseStartDragX;
    private double mouseStartDragY;

    private int cursorBlinkTicks = 0;

    private static final Color bgDark = new Color(12, 13, 18, 242);
    private static final Color headerDark = new Color(20, 22, 30, 250);
    private static final Color inputBg = new Color(18, 20, 28, 240);
    private static final Color gripInactive = new Color(100, 105, 125, 180);
    
    private static final int titleColor = new Color(220, 225, 240).getRGB();
    private static final int toggleColor = Color.LIGHT_GRAY.getRGB();
    
    private static final int textDefault = new Color(170, 175, 190).getRGB();
    private static final int textError = new Color(255, 95, 95).getRGB();
    private static final int textSuccess = new Color(95, 235, 135).getRGB();

    public TerminalWindow() {
        this.opened = true;
        this.hidden = false;
        this.resizing = false;
        this.dragging = false;
        this.focused = true;

        this.x = 15;
        this.y = 200;
        this.width = 280;
        this.height = 140;
        this.minWidth = 200;
        this.minHeight = 90;
        this.barHeight = 16;
        this.resizeHandleSize = 12;
        this.inputText = "";

        if (out.isEmpty()) {
            print("Raven B+ Console [v" + Raven.VERSION + "]", textSuccess);
            print("Type 'help' or 'list' for commands.", textDefault);
        }
    }

    public static void clearTerminal() {
        out.clear();
    }

    public static void print(String message) {
        int color = textDefault;
        if (message.startsWith("Error:") || message.startsWith("Unknown")) {
            color = textError;
        } else if (message.startsWith("Success:") || message.startsWith("Enabled") || message.startsWith("Toggled")) {
            color = textSuccess;
        } else if (message.startsWith("$ ")) {
            color = Theme.getMainColor().getRGB();
        }
        out.add(new TerminalLine(message, color));
    }

    private static void print(String message, int hexColor) {
        out.add(new TerminalLine(message, hexColor));
    }

    public void show() {
        this.hidden = false;
    }

    public void hide() {
        this.hidden = true;
    }

    @Override
    public void draw() {
        if (this.hidden) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        if (fr == null) return;

        this.cursorBlinkTicks++;
        Color accentColor = Theme.getMainColor();

        // Main Window Frame
        if (this.opened) {
            RoundedUtil.drawRound(this.x, this.y, this.width, this.height, 6f, bgDark);
        }

        // Header Bar
        RoundedUtil.drawRound(this.x, this.y, this.width, this.barHeight, 6f, headerDark);
        RoundedUtil.drawRound(this.x + 2, this.y + this.barHeight - 1, this.width - 4, 1.5f, 0.75f, accentColor);

        // Header Title
        fr.drawStringWithShadow(">_ Terminal", this.x + 8, this.y + 4, titleColor);

        // Header Toggle Button (- / +)
        String toggleSymbol = this.opened ? "−" : "+";
        fr.drawStringWithShadow(toggleSymbol, this.x + this.width - 14, this.y + 4, toggleColor);

        if (this.opened) {
            int inputBarHeight = 14;
            int inputBarY = this.y + this.height - inputBarHeight - 4;

            // Input Bar Card at Bottom
            RoundedUtil.drawRound(this.x + 4, inputBarY, this.width - 8, inputBarHeight, 3f, inputBg);

            int promptX = this.x + 8;
            int promptY = inputBarY + 3;
            fr.drawStringWithShadow("$ ", promptX, promptY, accentColor.getRGB());
            
            int textXOffset = fr.getStringWidth("$ ");
            fr.drawStringWithShadow(this.inputText, promptX + textXOffset, promptY, textDefault);
            
            if (this.focused && (this.cursorBlinkTicks / 20) % 2 == 0) {
                fr.drawStringWithShadow("_", promptX + textXOffset + fr.getStringWidth(this.inputText), promptY, accentColor.getRGB());
            }

            int contentStartY = this.y + this.barHeight + 4;
            int contentHeight = inputBarY - contentStartY - 3;
            int lineHeight = fr.FONT_HEIGHT + 2;

            if (contentHeight > 0) {
                int scaleFactor = getScaleFactor(mc);
                int scaledHeight = mc.displayHeight / scaleFactor;

                int scissorX = (this.x + 4) * scaleFactor;
                int scissorY = (scaledHeight - (contentStartY + contentHeight)) * scaleFactor;
                int scissorWidth = (this.width - 8) * scaleFactor;
                int scissorHeight = contentHeight * scaleFactor;

                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

                int maxVisibleLines = Math.max(1, contentHeight / lineHeight);
                int startIndex = Math.max(0, out.size() - maxVisibleLines);
                int currentY = contentStartY;

                for (int i = startIndex; i < out.size(); i++) {
                    TerminalLine line = out.get(i);
                    int renderColor = (line.color == Theme.getMainColor().getRGB()) ? accentColor.getRGB() : line.color;
                    
                    fr.drawStringWithShadow(line.text, this.x + 8, currentY, renderColor);
                    currentY += lineHeight;
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST);
            }

            int gripX = this.x + this.width - this.resizeHandleSize;
            int gripY = this.y + this.height - this.resizeHandleSize;
            Color gripColor = this.resizing ? accentColor : gripInactive;

            RoundedUtil.drawRound(gripX + 4, gripY + 8, 5f, 1.5f, 0.75f, gripColor);
            RoundedUtil.drawRound(gripX + 6, gripY + 5, 3f, 1.5f, 0.75f, gripColor);
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        if (this.hidden) return;

        if (this.dragging) {
            this.x = (int) (this.windowStartDragX + (mouseX - this.mouseStartDragX));
            this.y = (int) (this.windowStartDragY + (mouseY - this.mouseStartDragY));
        } else if (this.resizing && this.opened) {
            Minecraft mc = Minecraft.getMinecraft();
            int scaleFactor = getScaleFactor(mc);
            int scaledWidth = (int) Math.ceil((double) mc.displayWidth / scaleFactor);
            int scaledHeight = (int) Math.ceil((double) mc.displayHeight / scaleFactor);

            int newWidth = mouseX - this.x;
            int newHeight = mouseY - this.y;

            this.width = Math.max(this.minWidth, Math.min(scaledWidth - this.x - 5, newWidth));
            this.height = Math.max(this.minHeight, Math.min(scaledHeight - this.y - 5, newHeight));
        }
    }

    @Override
    public void mouseDown(int x, int y, int b) {
        if (this.hidden) return;

        if (overToggleButton(x, y) && b == 0) {
            this.opened = !this.opened;
            return;
        }

        if (overBar(x, y)) {
            if (b == 0) {
                this.dragging = true;
                this.mouseStartDragX = x;
                this.mouseStartDragY = y;
                this.windowStartDragX = this.x;
                this.windowStartDragY = this.y;
            } else if (b == 1) {
                this.opened = !this.opened;
            }
            this.focused = true;
            return;
        }

        if (this.opened && overResize(x, y) && b == 0) {
            this.resizing = true;
            this.focused = true;
            return;
        }

        if (overWindow(x, y)) {
            this.focused = true;
        } else {
            this.focused = false;
        }
    }

    @Override
    public void mouseReleased(int x, int y, int m) {
        this.dragging = false;
        this.resizing = false;
    }

    @Override
    public void keyTyped(char typedChar, int keyCode) {
        if (!this.focused || !this.opened) return;

        if (keyCode == Keyboard.KEY_RETURN) { // Enter Key
            if (!this.inputText.trim().isEmpty()) {
                String cmd = this.inputText.trim();
                print("$ " + cmd);
                commandHistory.add(cmd);
                this.historyIndex = commandHistory.size();
                processCommand(cmd);
                this.inputText = "";
            }
        } else if (keyCode == Keyboard.KEY_BACK) { // Backspace Key
            if (!this.inputText.isEmpty()) {
                this.inputText = this.inputText.substring(0, this.inputText.length() - 1);
            }
        } else if (keyCode == Keyboard.KEY_UP) { // History Previous
            if (!commandHistory.isEmpty() && historyIndex > 0) {
                historyIndex--;
                this.inputText = commandHistory.get(historyIndex);
            }
        } else if (keyCode == Keyboard.KEY_DOWN) { // History Next
            if (!commandHistory.isEmpty() && historyIndex < commandHistory.size() - 1) {
                historyIndex++;
                this.inputText = commandHistory.get(historyIndex);
            } else {
                historyIndex = commandHistory.size();
                this.inputText = "";
            }
        } else if (keyCode == Keyboard.KEY_TAB) { // Auto-complete
            handleAutoComplete();
        } else if (ChatAllowedKeys.isKeyAllowed(typedChar)) {
            this.inputText += typedChar;
        }
    }

    private void handleAutoComplete() {
        if (this.inputText.isEmpty()) return;
        String query = this.inputText.toLowerCase();

        for (Mod mod : Raven.moduleManager.getModules()) {
            if (mod.getName().toLowerCase().startsWith(query)) {
                this.inputText = mod.getName();
                return;
            }
        }
    }

    private void processCommand(String rawCmd) {
        String[] parts = rawCmd.split(" ");
        String cmd = parts[0].toLowerCase();

        switch (cmd) {
            case "help":
                print("Commands: clear, list, toggle <mod>, bind <mod> <key>, ping, config <name>, exit");
                break;
            case "clear":
            case "cls":
                clearTerminal();
                break;
            case "list":
                print("Registered Modules (" + Raven.moduleManager.numberOfModules() + "):");
                StringBuilder sb = new StringBuilder();
                for (Mod m : Raven.moduleManager.getModules()) {
                    sb.append(m.isEnabled() ? "§a" : "§7").append(m.getName()).append("§r, ");
                }
                print(sb.toString());
                break;
            case "toggle":
            case "t":
                if (parts.length > 1) {
                    Mod mod = Raven.moduleManager.getModuleByName(parts[1]);
                    if (mod != null) {
                        mod.toggle();
                        print("Success: Toggled " + mod.getName() + " -> " + (mod.isEnabled() ? "ENABLED" : "DISABLED"));
                    } else {
                        print("Error: Module '" + parts[1] + "' not found.");
                    }
                } else {
                    print("Usage: toggle <module>");
                }
                break;
            case "bind":
                if (parts.length > 2) {
                    Mod mod = Raven.moduleManager.getModuleByName(parts[1]);
                    if (mod != null) {
                        int key = Keyboard.getKeyIndex(parts[2].toUpperCase());
                        mod.setKeycode(key);
                        print("Success: Bound " + mod.getName() + " to " + Keyboard.getKeyName(key));
                    } else {
                        print("Error: Module '" + parts[1] + "' not found.");
                    }
                } else {
                    print("Usage: bind <module> <key>");
                }
                break;
            case "ping":
                ChatUtil.checkPing();
                print("Checking ping...");
                break;
            case "config":
                if (parts.length > 1) {
                    ConfigManager.loadConfigByName(parts[1]);
                    print("Success: Loaded config '" + parts[1] + "'");
                } else {
                    print("Current config: " + ConfigManager.getCurrentProfileName());
                }
                break;
            case "exit":
            case "close":
                this.opened = false;
                break;
            default:
                boolean handled = CommandManager.execute("." + rawCmd);
                if (!handled) {
                    print("Unknown command: '" + cmd + "'. Type 'help' for commands.");
                }
                break;
        }
    }

    @Override
    public void setComponentStartAt(int n) {}

    @Override
    public int height() {
        return this.height;
    }

    public boolean overPosition(int x, int y) {
        return !this.hidden && (this.opened ? overWindow(x, y) : overBar(x, y));
    }

    public boolean overBar(int x, int y) {
        return x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
    }

    public boolean overWindow(int x, int y) {
        return this.opened && x >= this.x && x <= this.x + this.width && y >= this.y && y <= this.y + this.height;
    }

    public boolean overResize(int x, int y) {
        return this.opened && x >= this.x + this.width - this.resizeHandleSize && x <= this.x + this.width
                && y >= this.y + this.height - this.resizeHandleSize && y <= this.y + this.height;
    }

    public boolean overToggleButton(int x, int y) {
        return x >= this.x + this.width - 18 && x <= this.x + this.width && y >= this.y && y <= this.y + this.barHeight;
    }

    public void setLocation(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public boolean hidden() {
        return this.hidden;
    }

    public int getY() {
        return this.y;
    }

    public int getX() {
        return this.x;
    }

    public int getWidth() {
        return this.width;
    }

    private int getScaleFactor(Minecraft mc) {
        int scaleFactor = 1;
        int guiScale = mc.gameSettings.guiScale;
        if (guiScale == 0) guiScale = 1000;
        while (scaleFactor < guiScale && mc.displayWidth / (scaleFactor + 1) >= 320 && mc.displayHeight / (scaleFactor + 1) >= 240) {
            ++scaleFactor;
        }
        return scaleFactor;
    }

    private static class TerminalLine {
        public final String text;
        public final int color;

        public TerminalLine(String text, int color) {
            this.text = text;
            this.color = color;
        }
    }

    private static class ChatAllowedKeys {
        public static boolean isKeyAllowed(char c) {
            return c >= 32 && c != 127;
        }
    }
}