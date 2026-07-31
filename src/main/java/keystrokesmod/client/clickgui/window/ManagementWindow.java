package keystrokesmod.client.clickgui.window;

import java.awt.Color;
import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.component.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.config.ConfigManager;
import keystrokesmod.client.module.modules.client.GuiModule;
import keystrokesmod.client.util.render.RoundedUtil;
import keystrokesmod.client.util.system.EnemyManager;
import keystrokesmod.client.util.system.FriendManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

public class ManagementWindow extends Component {
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
    protected boolean focused;
    private boolean scrollingBar;

    private int scrollOffset = 0;
    private int maxScroll = 0;

    private int activeTab = 0; // 0: Themes, 1: Configs, 2: Friends/Enemies
    private final String[] tabs = new String[] { "Themes", "Configs", "Social" };

    private double windowStartDragX;
    private double windowStartDragY;
    private double mouseStartDragX;
    private double mouseStartDragY;

    private final List<String> availableThemes = new ArrayList<>();
    private final List<String> cachedProfiles = new ArrayList<>();
    private GuiModule guiModule;
    private long lastProfileUpdate = 0L;

    private static final Color bgDarkColor = new Color(13, 14, 19, 240);
    private static final Color headerDarkColor = new Color(22, 24, 32, 250);
    private static final Color outlineColor = new Color(45, 48, 65, 200);
    private static final Color tabBgInactiveColor = new Color(25, 27, 36, 200);
    private static final Color gripInactive = new Color(100, 105, 125, 180);
    
    private static final Color themeActiveCardColor = new Color(32, 35, 48, 230);
    private static final Color cardNormalColor = new Color(18, 19, 26, 200);
    private static final Color configActiveCardColor = new Color(35, 38, 55, 235);
    private static final Color friendCardColor = new Color(18, 28, 22, 200);
    private static final Color enemyCardColor = new Color(28, 18, 18, 200);
    
    private static final int titleColorRgb = new Color(220, 225, 240).getRGB();
    private static final int toggleColorRgb = Color.LIGHT_GRAY.getRGB();
    private static final int tabTextInactiveRgb = new Color(150, 155, 170).getRGB();
    private static final int themeTextInactiveRgb = new Color(150, 155, 170).getRGB();
    private static final int configTextInactiveRgb = new Color(170, 175, 190).getRGB();
    private static final int friendTitleColorRgb = new Color(100, 240, 140).getRGB();
    private static final int friendTextColorRgb = new Color(120, 245, 160).getRGB();
    private static final int enemyTitleColorRgb = new Color(255, 100, 100).getRGB();
    private static final int enemyTextColorRgb = new Color(255, 120, 120).getRGB();
    private static final int emptySocialColorRgb = new Color(130, 135, 145).getRGB();

    private static final Color pastelPinkColor = new Color(237, 138, 209);
    private static final Color cherryColor = new Color(255, 200, 200);
    private static final Color maiColor = new Color(57, 46, 126);
    private static final Color sassanColor = new Color(255, 105, 105);
    private static final Color goldColor = new Color(255, 215, 0);
    private static final Color steelColor = new Color(52, 152, 219);
    private static final Color emeraldColor = new Color(46, 204, 113);
    private static final Color orangeColor = new Color(255, 165, 0);
    private static final Color amethystColor = new Color(155, 89, 182);
    private static final Color lilyColor = new Color(76, 56, 108);

    public ManagementWindow() {
        this.x = 310;
        this.y = 15;
        this.width = 240;
        this.height = 180;
        this.minWidth = 200;
        this.minHeight = 130;
        this.barHeight = 16;
        this.resizeHandleSize = 12;

        this.opened = true;
        this.hidden = false;

        for (GuiModule.Colors color : GuiModule.Colors.values()) {
            availableThemes.add(color.name());
        }
        updateProfilesCache();
    }

    private GuiModule getGuiModule() {
        if (guiModule == null) {
            guiModule = (GuiModule) Raven.moduleManager.getModuleByClazz(GuiModule.class);
        }
        return guiModule;
    }

    private void updateProfilesCache() {
        long currentTime = System.currentTimeMillis();
        if (currentTime - lastProfileUpdate < 2000L) {
            return;
        }
        lastProfileUpdate = currentTime;
        
        cachedProfiles.clear();
        cachedProfiles.add("default");
        
        File[] profiles = ConfigManager.PROFILES_DIR.listFiles((dir, name) -> name.endsWith(".stela"));
        if (profiles != null) {
            for (File f : profiles) {
                String name = f.getName();
                String pName = name.substring(0, name.length() - 6); 
                if (!cachedProfiles.contains(pName)) {
                    cachedProfiles.add(pName);
                }
            }
        }
    }

    @Override
    public void draw() {
        if (this.hidden) return;

        Minecraft mc = Minecraft.getMinecraft();
        FontRenderer fr = mc.fontRendererObj;
        if (fr == null) return;

        this.maxScroll = 0;

        Color accentColor = Theme.getMainColor();
        final int accentColorRgb = accentColor.getRGB();

        // Independent Floating Window Frame with Shader Outline
        if (this.opened) {
            RoundedUtil.drawRoundOutline(this.x, this.y, this.width, this.height, 6f, 1f, bgDarkColor, outlineColor);
        }

        // Header Bar
        RoundedUtil.drawRound(this.x, this.y, this.width, this.barHeight, 6f, headerDarkColor);
        RoundedUtil.drawRound(this.x + 2, this.y + this.barHeight - 1, this.width - 4, 1.5f, 0.75f, accentColor);

        // Window Title
        fr.drawStringWithShadow("Management", this.x + 8, this.y + 4, titleColorRgb);

        // Header Toggle Button (- / +)
        String toggleSymbol = this.opened ? "−" : "+";
        fr.drawStringWithShadow(toggleSymbol, this.x + this.width - 14, this.y + 4, toggleColorRgb);

        if (this.opened) {
            // Render Tab Navigation Bar
            int tabWidth = (this.width - 16) / tabs.length;
            int tabY = this.y + this.barHeight + 4;

            for (int i = 0; i < tabs.length; i++) {
                int tabX = this.x + 8 + (i * tabWidth);
                boolean selected = (i == activeTab);

                Color tabBg = selected ? accentColor : tabBgInactiveColor;
                RoundedUtil.drawRound(tabX, tabY, tabWidth - 2, 13f, 2.5f, tabBg);

                int textW = fr.getStringWidth(tabs[i]);
                fr.drawStringWithShadow(
                    tabs[i],
                    tabX + (tabWidth - 2 - textW) / 2f,
                    tabY + 3,
                    selected ? Color.WHITE.getRGB() : tabTextInactiveRgb
                );
            }

            int contentY = tabY + 18;
            int contentHeight = (this.y + this.height - 10) - contentY;

            // Content Area with Scissor Test
            if (contentHeight > 0) {
                int totalContentHeight = 0;
                if (activeTab == 0) {
                    totalContentHeight = availableThemes.size() * 16;
                } else if (activeTab == 1) {
                    updateProfilesCache();
                    totalContentHeight = cachedProfiles.size() * 16;
                } else if (activeTab == 2) {
                    totalContentHeight = 28 + (FriendManager.getFriends().size() * 16) + 28 + (EnemyManager.getEnemies().size() * 16);
                }

                maxScroll = Math.max(0, totalContentHeight - contentHeight);
                if (scrollOffset > maxScroll) scrollOffset = maxScroll;
                if (scrollOffset < 0) scrollOffset = 0;

                ScaledResolution sr = new ScaledResolution(mc);
                int scaleFactor = sr.getScaleFactor();

                int scissorX = (this.x + 4) * scaleFactor;
                int scissorY = (sr.getScaledHeight() - (contentY + contentHeight)) * scaleFactor;
                int scissorWidth = (this.width - 8) * scaleFactor;
                int scissorHeight = contentHeight * scaleFactor;

                GL11.glEnable(GL11.GL_SCISSOR_TEST);
                GL11.glScissor(scissorX, scissorY, scissorWidth, scissorHeight);

                int startY = contentY - scrollOffset;
                if (activeTab == 0) {
                    drawThemesTab(fr, startY);
                } else if (activeTab == 1) {
                    drawConfigsTab(fr, startY, accentColorRgb);
                } else if (activeTab == 2) {
                    drawSocialTab(fr, startY);
                }

                GL11.glDisable(GL11.GL_SCISSOR_TEST);

                // Draw Scrollbar
                if (maxScroll > 0) {
                    int trackX = this.x + this.width - 7;
                    int trackY = contentY;
                    int trackWidth = 3;
                    int trackHeight = contentHeight;

                    RoundedUtil.drawRound(trackX, trackY, trackWidth, trackHeight, 1.5f, new Color(25, 28, 38, 150));

                    int thumbHeight = Math.max(12, (contentHeight * contentHeight) / totalContentHeight);
                    int thumbY = trackY + (int) ((float) scrollOffset / maxScroll * (contentHeight - thumbHeight));

                    Color thumbColor = this.scrollingBar ? accentColor : new Color(100, 105, 125, 200);
                    RoundedUtil.drawRound(trackX, thumbY, trackWidth, thumbHeight, 1.5f, thumbColor);
                }
            }

            // Resize Handle Grip
            int gripX = this.x + this.width - this.resizeHandleSize;
            int gripY = this.y + this.height - this.resizeHandleSize;
            Color gripColor = this.resizing ? accentColor : gripInactive;

            RoundedUtil.drawRound(gripX + 4, gripY + 8, 5f, 1.5f, 0.75f, gripColor);
            RoundedUtil.drawRound(gripX + 6, gripY + 5, 3f, 1.5f, 0.75f, gripColor);
        }
    }

    private void drawThemesTab(FontRenderer fr, int startY) {
        GuiModule gui = getGuiModule();
        String activeTheme = (gui != null) ? gui.mode.getMode() : "PastelPink";
        int currentY = startY;

        for (int i = 0; i < availableThemes.size(); i++) {
            String themeName = availableThemes.get(i);
            boolean isCurrent = themeName.equalsIgnoreCase(activeTheme);
            Color themeColor = getThemeColorByName(themeName);

            Color cardBg = isCurrent ? themeActiveCardColor : cardNormalColor;
            RoundedUtil.drawRound(this.x + 8, currentY, this.width - 16, 14f, 3f, cardBg);
            RoundedUtil.drawRound(this.x + 12, currentY + 3, 8f, 8f, 2f, themeColor);

            fr.drawStringWithShadow(
                themeName,
                this.x + 26,
                currentY + 3,
                isCurrent ? Color.WHITE.getRGB() : themeTextInactiveRgb
            );

            currentY += 16;
        }
    }

    private void drawConfigsTab(FontRenderer fr, int startY, int accentColorRgb) {
        updateProfilesCache();
        String currentProfile = ConfigManager.getCurrentProfileName();
        int currentY = startY;

        for (int i = 0; i < cachedProfiles.size(); i++) {
            String pName = cachedProfiles.get(i);
            boolean isSelected = pName.equalsIgnoreCase(currentProfile);
            Color cardBg = isSelected ? configActiveCardColor : cardNormalColor;
            
            RoundedUtil.drawRound(this.x + 8, currentY, this.width - 16, 14f, 3f, cardBg);

            fr.drawStringWithShadow(
                pName,
                this.x + 12,
                currentY + 3,
                isSelected ? accentColorRgb : configTextInactiveRgb
            );

            currentY += 16;
        }
    }

    private void drawSocialTab(FontRenderer fr, int startY) {
        int currentY = startY;

        Set<String> friends = FriendManager.getFriends();
        fr.drawStringWithShadow("Friends (" + friends.size() + ")", this.x + 8, currentY, friendTitleColorRgb);
        currentY += 12;

        if (friends.isEmpty()) {
            fr.drawStringWithShadow("No friends added", this.x + 12, currentY, emptySocialColorRgb);
            currentY += 14;
        } else {
            for (String friend : friends) {
                RoundedUtil.drawRound(this.x + 8, currentY, this.width - 16, 14f, 3f, friendCardColor);
                fr.drawStringWithShadow(friend, this.x + 12, currentY + 3, friendTextColorRgb);
                currentY += 16;
            }
        }

        currentY += 4;
        Set<String> enemies = EnemyManager.getEnemies();
        fr.drawStringWithShadow("Enemies (" + enemies.size() + ")", this.x + 8, currentY, enemyTitleColorRgb);
        currentY += 12;

        if (enemies.isEmpty()) {
            fr.drawStringWithShadow("No enemies added", this.x + 12, currentY, emptySocialColorRgb);
        } else {
            for (String enemy : enemies) {
                RoundedUtil.drawRound(this.x + 8, currentY, this.width - 16, 14f, 3f, enemyCardColor);
                fr.drawStringWithShadow(enemy, this.x + 12, currentY + 3, enemyTextColorRgb);
                currentY += 16;
            }
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
            ScaledResolution sr = new ScaledResolution(mc);

            int newWidth = mouseX - this.x;
            int newHeight = mouseY - this.y;

            this.width = Math.max(this.minWidth, Math.min(sr.getScaledWidth() - this.x - 5, newWidth));
            this.height = Math.max(this.minHeight, Math.min(sr.getScaledHeight() - this.y - 5, newHeight));
        } else if (this.scrollingBar && this.opened && maxScroll > 0) {
            int tabY = this.y + this.barHeight + 4;
            int contentY = tabY + 18;
            int contentHeight = (this.y + this.height - 10) - contentY;

            float mouseRelativeY = mouseY - contentY;
            float scrollFraction = Math.max(0.0f, Math.min(1.0f, mouseRelativeY / contentHeight));
            this.scrollOffset = (int) (scrollFraction * maxScroll);
        }
    }

    @Override
    public void mouseDown(int x, int y, int b) {
        if (this.hidden) return;

        if (this.opened && maxScroll > 0 && b == 0) {
            int tabY = this.y + this.barHeight + 4;
            int contentY = tabY + 18;
            int contentHeight = (this.y + this.height - 10) - contentY;
            int trackX = this.x + this.width - 9;

            if (x >= trackX && x <= this.x + this.width && y >= contentY && y <= contentY + contentHeight) {
                this.scrollingBar = true;
                return;
            }
        }

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

        if (this.opened) {
            int tabWidth = (this.width - 16) / tabs.length;
            int tabY = this.y + this.barHeight + 4;

            for (int i = 0; i < tabs.length; i++) {
                int tabX = this.x + 8 + (i * tabWidth);
                if (x >= tabX && x <= tabX + tabWidth - 2 && y >= tabY && y <= tabY + 13) {
                    this.activeTab = i;
                    return;
                }
            }

            int contentY = tabY + 18;

            if (activeTab == 0 && b == 0) { // Themes
                int currentY = contentY - scrollOffset;
                GuiModule gui = getGuiModule();
                for (int i = 0; i < availableThemes.size(); i++) {
                    String themeName = availableThemes.get(i);
                    if (x >= this.x + 8 && x <= this.x + this.width - 12 && y >= Math.max(contentY, currentY) && y <= Math.min(contentY + (this.y + this.height - 10 - contentY), currentY + 14)) {
                        if (gui != null) {
                            gui.mode.setMode(themeName);
                            Theme.invalidateCache();
                        }
                        return;
                    }
                    currentY += 16;
                }
            } else if (activeTab == 1 && b == 0) { // Configs
                updateProfilesCache();
                int currentY = contentY - scrollOffset;
                for (int i = 0; i < cachedProfiles.size(); i++) {
                    String pName = cachedProfiles.get(i);
                    if (x >= this.x + 8 && x <= this.x + this.width - 12 && y >= Math.max(contentY, currentY) && y <= Math.min(contentY + (this.y + this.height - 10 - contentY), currentY + 14)) {
                        ConfigManager.loadConfigByName(pName);
                        return;
                    }
                    currentY += 16;
                }
            }
        }
    }

    @Override
    public void mouseReleased(int x, int y, int m) {
        this.dragging = false;
        this.resizing = false;
        this.scrollingBar = false;
    }

    public void handleMouseInput(int dWheel) {
        if (this.opened && maxScroll > 0) {
            if (dWheel != 0) {
                scrollOffset += (dWheel > 0 ? -18 : 18);
                if (scrollOffset < 0) scrollOffset = 0;
                if (scrollOffset > maxScroll) scrollOffset = maxScroll;
            }
        }
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

    private Color getThemeColorByName(String name) {
        switch (name) {
            case "PastelPink": return pastelPinkColor;
            case "Cherry": return cherryColor;
            case "Mai": return maiColor;
            case "Sassan": return sassanColor;
            case "Gold": return goldColor;
            case "Steel": return steelColor;
            case "Emerald": return emeraldColor;
            case "Orange": return orangeColor;
            case "Amethyst": return amethystColor;
            case "Lily": return lilyColor;
            default: return Color.WHITE;
        }
    }
}