package wtf.clickgui.window;

import java.awt.Color;
import java.io.IOException;

import org.lwjgl.input.Keyboard;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.ChatAllowedCharacters;
import net.minecraft.util.Session;
import wtf.clickgui.theme.Theme;
import wtf.util.render.RoundedUtil;
import wtf.util.system.ReflectUtil;

public class GuiAltManager extends GuiScreen {
    private final GuiScreen previousScreen;
    
    // Componentes Custom
    private CustomTextField usernameField;
    private CustomTextField tokenField;
    private CustomButton offlineButton;
    private CustomButton premiumButton;
    private CustomButton backButton;
    
    private String status = "Waiting for login...";
    private Color statusColor = Color.LIGHT_GRAY;

    // Paleta de colores consistente
    private static final Color bgDark = new Color(13, 14, 19, 240);
    private static final Color fieldBg = new Color(25, 27, 36, 200);
    private static final Color fieldOutline = new Color(45, 48, 65, 255);
    private static final Color buttonBg = new Color(32, 35, 48, 230);
    private static final Color buttonHover = new Color(45, 48, 65, 250);

    public GuiAltManager(GuiScreen previousScreen) {
        this.previousScreen = previousScreen;
    }

    @Override
    public void initGui() {
        Keyboard.enableRepeatEvents(true);

        int centerX = this.width / 2;
        int centerY = this.height / 2;

        this.usernameField = new CustomTextField(centerX - 100, centerY - 35, 200, 20, "Username / Email");
        this.usernameField.setFocused(true);
        
        this.tokenField = new CustomTextField(centerX - 100, centerY + 5, 200, 20, "Token (Only for Premium)");

        this.offlineButton = new CustomButton(centerX - 100, centerY + 40, 95, 20, "Login (Offline)");
        this.premiumButton = new CustomButton(centerX + 5, centerY + 40, 95, 20, "Login (Premium)");
        this.backButton = new CustomButton(centerX - 100, centerY + 65, 200, 20, "Back");
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        this.drawDefaultBackground();
        
        int centerX = this.width / 2;
        int centerY = this.height / 2;

        RoundedUtil.drawRoundOutline(centerX - 120, centerY - 80, 240, 175, 8f, 1f, bgDark, fieldOutline);

        this.fontRendererObj.drawStringWithShadow("Alt Manager", centerX - this.fontRendererObj.getStringWidth("Alt Manager") / 2f, centerY - 70, Color.WHITE.getRGB());
        this.fontRendererObj.drawStringWithShadow(status, centerX - this.fontRendererObj.getStringWidth(status) / 2f, centerY - 58, statusColor.getRGB());

        this.usernameField.draw(mouseX, mouseY);
        this.tokenField.draw(mouseX, mouseY);
        
        this.offlineButton.draw(mouseX, mouseY);
        this.premiumButton.draw(mouseX, mouseY);
        this.backButton.draw(mouseX, mouseY);

        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void keyTyped(char typedChar, int keyCode) throws IOException {
        if (keyCode == Keyboard.KEY_ESCAPE) {
            this.mc.displayGuiScreen(this.previousScreen);
            return;
        }

        // Navegación con TAB
        if (keyCode == Keyboard.KEY_TAB) {
            this.usernameField.setFocused(!this.usernameField.isFocused());
            this.tokenField.setFocused(!this.tokenField.isFocused());
            return;
        }
        
        this.usernameField.keyTyped(typedChar, keyCode);
        this.tokenField.keyTyped(typedChar, keyCode);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        if (mouseButton == 0) {
            this.usernameField.mouseClicked(mouseX, mouseY);
            this.tokenField.mouseClicked(mouseX, mouseY);
            
            if (this.offlineButton.isHovered(mouseX, mouseY)) {
                loginOffline();
            } else if (this.premiumButton.isHovered(mouseX, mouseY)) {
                loginPremium();
            } else if (this.backButton.isHovered(mouseX, mouseY)) {
                this.mc.displayGuiScreen(this.previousScreen);
            }
        }
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    private void loginOffline() {
        if (!this.usernameField.getText().isEmpty()) {
            ReflectUtil.setSession(new Session(this.usernameField.getText(), "", "0", "legacy"));
            this.status = "Logged in as (Offline): " + this.usernameField.getText();
            this.statusColor = new Color(100, 240, 140); // Verde
        } else {
            this.status = "Username cannot be empty!";
            this.statusColor = new Color(255, 100, 100); // Rojo
        }
    }

    private void loginPremium() {
        if (!this.usernameField.getText().isEmpty() && !this.tokenField.getText().isEmpty()) {
            ReflectUtil.setSession(new Session(this.usernameField.getText(), "premium_uuid_placeholder", this.tokenField.getText(), "mojang"));
            this.status = "Logged in as (Premium): " + this.usernameField.getText();
            this.statusColor = new Color(100, 240, 140);
        } else {
            this.status = "Username and Token are required!";
            this.statusColor = new Color(255, 100, 100);
        }
    }

    @Override
    public void onGuiClosed() {
        Keyboard.enableRepeatEvents(false);
    }

    private class CustomTextField {
        private final int x, y, width, height;
        private final String placeholder;
        private String text = "";
        private boolean focused;

        public CustomTextField(int x, int y, int width, int height, String placeholder) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.placeholder = placeholder;
        }

        public void draw(int mouseX, int mouseY) {
            if (focused) {
                RoundedUtil.drawRoundOutline(x, y, width, height, 4f, 1f, fieldBg, Theme.getMainColor());
            } else {
                RoundedUtil.drawRound(x, y, width, height, 4f, fieldBg);
            }

            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            if (text.isEmpty() && !focused) {
                fr.drawStringWithShadow(placeholder, x + 6, y + (height - 8) / 2f, Color.GRAY.getRGB());
            } else {
                String displayText = text;
                while (fr.getStringWidth(displayText) > width - 14) {
                    displayText = displayText.substring(1);
                }
                
                String cursor = (focused && System.currentTimeMillis() % 1000 < 500) ? "_" : "";
                fr.drawStringWithShadow(displayText + cursor, x + 6, y + (height - 8) / 2f, Color.WHITE.getRGB());
            }
            
            fr.drawStringWithShadow(placeholder, x + 2, y - 10, Color.GRAY.getRGB());
        }

        public void keyTyped(char typedChar, int keyCode) {
            if (!focused) return;
            
            if (GuiScreen.isCtrlKeyDown() && keyCode == Keyboard.KEY_V) {
                String clipboard = GuiScreen.getClipboardString();
                if (clipboard != null) {
                    text += clipboard.replaceAll("\n", "").replaceAll("\r", "");
                }
                return;
            }

            if (keyCode == Keyboard.KEY_BACK && text.length() > 0) {
                text = text.substring(0, text.length() - 1);
            } else if (ChatAllowedCharacters.isAllowedCharacter(typedChar)) {
                text += typedChar;
            }
        }

        public void mouseClicked(int mouseX, int mouseY) {
            this.focused = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }

        public String getText() { return text; }
        public boolean isFocused() { return focused; }
        public void setFocused(boolean focused) { this.focused = focused; }
    }

    private class CustomButton {
        private final int x, y, width, height;
        private final String text;

        public CustomButton(int x, int y, int width, int height, String text) {
            this.x = x;
            this.y = y;
            this.width = width;
            this.height = height;
            this.text = text;
        }

        public void draw(int mouseX, int mouseY) {
            boolean hovered = isHovered(mouseX, mouseY);
            Color renderColor = hovered ? buttonHover : buttonBg;

            RoundedUtil.drawRound(x, y, width, height, 4f, renderColor);
            
            FontRenderer fr = Minecraft.getMinecraft().fontRendererObj;
            fr.drawStringWithShadow(text, x + (width - fr.getStringWidth(text)) / 2f, y + (height - 8) / 2f, Color.WHITE.getRGB());
        }

        public boolean isHovered(int mouseX, int mouseY) {
            return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
        }
    }
}