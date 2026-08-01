package keystrokesmod.client.module.modules.client;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.clickgui.ClickGui;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.DragEvent;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.render.ColorUtil;
import keystrokesmod.client.util.render.RenderUtil;
import keystrokesmod.client.util.render.RoundedUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.settings.GameSettings;

@ModuleInfo(name = "HUD", category = Category.Client)
public class HUD extends Mod {

    public final ModeValue mode = new ModeValue("Color Mode", this, ColourModes.ASTOLFO2, ColourModes.values());
    
    public final BooleanValue showBackground = new BooleanValue("Background", this, true);
    public final ModeValue backgroundColor = new ModeValue("Bg Mode", this, () -> showBackground.getValue(), "Dark Glass", "Dark Glass", "Glass", "Gradient", "Outline", "Flat");
    public final NumberValue bgOpacity = new NumberValue("Bg Opacity", this, 160.0, 0.0, 255.0, 5.0, () -> showBackground.getValue());
    public final NumberValue padding = new NumberValue("Padding", this, 4.0, 1.0, 10.0, 0.5, () -> showBackground.getValue());

    // Accent bar settings
    public final ModeValue accentBar = new ModeValue("Accent Bar", this, "Right", new String[]{"Right", "Left", "None"});
    public final NumberValue barWidth = new NumberValue("Bar Width", this, 2.0, 1.0, 5.0, 0.5, () -> !accentBar.is("None"));

    public final BooleanValue alphabeticalSort = new BooleanValue("Alphabetical sort", this, false);

    protected final DescriptionValue desc = new DescriptionValue("Hide Category", this);
    public final BooleanValue hideClient = new BooleanValue("Hide Client", this, false);
    public final BooleanValue hideCombat = new BooleanValue("Hide Combat", this, false);
    public final BooleanValue hideMovement = new BooleanValue("Hide Movement", this, false);
    public final BooleanValue hideOther = new BooleanValue("Hide Other", this, false);
    public final BooleanValue hidePlayer = new BooleanValue("Hide Player", this, false);
    public final BooleanValue hideRender = new BooleanValue("Hide Render", this, false);

    public static int hudX = 5;
    public static int hudY = 70;
    public static PositionMode positionMode = PositionMode.UPLEFT;

    private static boolean draggingModuleList = false;
    private static float dragOffsetX = 0f;
    private static float dragOffsetY = 0f;

    private final List<Mod> renderList = new ArrayList<>(64);
    
    private float cachedMaxWidth = 0;
    private float cachedTotalHeight = 0;

    private ScaledResolution cachedSR;
    private int lastDisplayWidth = -1, lastDisplayHeight = -1, lastGuiScale = -1;

    private final Comparator<Mod> sortLongShort = (m1, m2) -> Float.compare(getStringWidth(m2.getName()), getStringWidth(m1.getName()));
    private final Comparator<Mod> sortShortLong = (m1, m2) -> Float.compare(getStringWidth(m1.getName()), getStringWidth(m2.getName()));
    private final Comparator<Mod> sortAlphabetical = Comparator.comparing(Mod::getName);

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc != null && getFont() != null) {
            Kevin.moduleManager.sort();
        }
    }

    @Override
    public void guiButtonToggled(final BooleanValue setting) {
        super.guiButtonToggled(setting);
        if (setting == alphabeticalSort && mc != null && getFont() != null) {
            Kevin.moduleManager.sort();
        }
    }

    @EventLink
    private Listener<PostRenderTickEvent> onDraw = event -> {
        if (checkGame() || gameSetting().showDebugInfo || currentScreen() instanceof ClickGui) return;

        renderList.clear();
        for (Mod m : Kevin.moduleManager.getModules()) {
            if (m.isEnabled() && m != this && !isCategoryHidden(m.moduleCategory())) {
                renderList.add(m);
            }
        }

        if (renderList.isEmpty()) return;

        if (alphabeticalSort.getValue()) {
            renderList.sort(sortAlphabetical);
        } else {
            renderList.sort(positionMode.isTop() ? sortLongShort : sortShortLong);
        }

        final float fontHeight = getFontHeight();
        final float pad = showBackground.getValue() ? (float) padding.getValue() : 0.0f;
        final float lineHeight = fontHeight + 2f;
        final int alpha = (int) bgOpacity.getValue();
        final boolean rightAligned = positionMode.isRight();

        float maxWidth = 0;
        for (int i = 0; i < renderList.size(); i++) {
            float width = getStringWidth(renderList.get(i).getName());
            if (width > maxWidth) maxWidth = width;
        }

        float totalHeight = renderList.size() * lineHeight;
        
        cachedMaxWidth = maxWidth + pad * 2;
        cachedTotalHeight = totalHeight;

        ScaledResolution sr = getScaledResolution();
        int margin = 2;
        int correctedX = Math.max(hudX, margin);
        int correctedY = Math.max(hudY, margin);
        correctedX = (int) Math.min(correctedX, sr.getScaledWidth() - cachedMaxWidth - margin);
        correctedY = (int) Math.min(correctedY, sr.getScaledHeight() - cachedTotalHeight - margin);

        hudX = correctedX;
        hudY = correctedY;

        float y = hudY;
        int del = 0;

        for (int i = 0; i < renderList.size(); i++) {
            Mod m = renderList.get(i);
            float textW = getStringWidth(m.getName());
            
            float lineBgW = textW + pad * 2;
            float lineBgX = rightAligned ? hudX + (maxWidth - textW) - pad : hudX;
            float textX = lineBgX + pad;
            float textY = y + 1f;
            int color = getColorForMode(mode, del);

            if (showBackground.getValue()) {
                renderPerModuleBg(lineBgX, y, lineBgW, lineHeight, alpha, del);

                if (!accentBar.is("None")) {
                    float bWidth = (float) barWidth.getValue();
                    Color accentCol = new Color(color);
                    if (accentBar.is("Right")) {
                        RenderUtil.drawRect(lineBgX + lineBgW, y, lineBgX + lineBgW + bWidth, y + lineHeight, accentCol.getRGB());
                    } else if (accentBar.is("Left")) {
                        RenderUtil.drawRect(lineBgX - bWidth, y, lineBgX, y + lineHeight, accentCol.getRGB());
                    }
                }
            }

            drawString(m.getName(), textX, textY, color, true);

            y += lineHeight;
            del -= getDeltaForMode(mode);
        }
    };

    private void renderPerModuleBg(float x, float y, float w, float h, int alpha, int del) {
        switch (backgroundColor.getMode()) {
            case "Glass":
                Color glassBg = new Color(20, 20, 25, Math.min(255, alpha));
                RenderUtil.drawRect(x, y, x + w, y + h, glassBg.getRGB());
                break;
            case "Dark Glass":
                Color darkBg = new Color(12, 13, 18, Math.min(255, alpha));
                RenderUtil.drawRect(x, y, x + w, y + h, darkBg.getRGB());
                break;
            case "Gradient":
                Color topCol = ColorUtil.reAlpha(new Color(getColorForMode(mode, del)), Math.min(255, alpha));
                Color botCol = ColorUtil.reAlpha(new Color(getColorForMode(mode, del - 20)), Math.min(255, alpha));
                RoundedUtil.drawGradientVertical(x, y, w, h, 0f, topCol, botCol);
                break;
            case "Outline":
                Color outlineBg = new Color(15, 15, 20, Math.min(255, alpha));
                Color strokeCol = new Color(getColorForMode(mode, del));
                RenderUtil.drawRect(x, y, x + w, y + h, outlineBg.getRGB());
                RenderUtil.drawRect(x, y, x + 1, y + h, strokeCol.getRGB());
                break;
            default:
                Color defaultBg = new Color(0, 0, 0, Math.min(255, alpha));
                RenderUtil.drawRect(x, y, x + w, y + h, defaultBg.getRGB());
                break;
        }
    }

    @EventLink
    private Listener<DragEvent> onDrag = event -> {
        if (cachedMaxWidth == 0 || cachedTotalHeight == 0) return;

        final int mouseX = event.mouseX;
        final int mouseY = event.mouseY;
        boolean mouseDown = GameSettings.isKeyDown(gameSetting().keyBindAttack);

        if (mouseDown) {
            boolean hovering = mouseX >= hudX && mouseX <= hudX + cachedMaxWidth
                    && mouseY >= hudY && mouseY <= hudY + cachedTotalHeight;

            if (hovering && !draggingModuleList) {
                draggingModuleList = true;
                dragOffsetX = mouseX - hudX;
                dragOffsetY = mouseY - hudY;
            }

            if (draggingModuleList) {
                hudX = (int) (mouseX - dragOffsetX);
                hudY = (int) (mouseY - dragOffsetY);
            }
        } else {
            draggingModuleList = false;
        }
    };
    
    public FontRenderer getFont() {
    	return mc.fontRendererObj;
    }

    public float getStringWidth(String text) {
        return getFont() != null ? getFont().getStringWidth(text) : 0;
    }

    public float getFontHeight() {
        return getFont() != null ? getFont().FONT_HEIGHT : 9;
    }

    public void drawString(String text, float x, float y, int color, boolean shadow) {
        if (getFont() != null) {
            getFont().drawString(text, x, y, color, shadow);
        }
    }

    private ScaledResolution getScaledResolution() {
        int scale = gameSetting().guiScale;
        if (cachedSR == null || lastDisplayWidth != mc.displayWidth || lastDisplayHeight != mc.displayHeight || lastGuiScale != scale) {
            cachedSR = new ScaledResolution(mc);
            lastDisplayWidth = mc.displayWidth;
            lastDisplayHeight = mc.displayHeight;
            lastGuiScale = scale;
        }
        return cachedSR;
    }

    private boolean isCategoryHidden(Category category) {
        switch (category) {
            case Client: return hideClient.getValue();
            case Combat: return hideCombat.getValue();
            case Movement: return hideMovement.getValue();
            case Player: return hidePlayer.getValue();
            case Render: return hideRender.getValue();
            case Other: return hideOther.getValue();
            default: return false;
        }
    }

    private int getColorForMode(ModeValue mode, int del) {
        if (mode.is(ColourModes.RAVEN) || mode.is(ColourModes.RAVEN2))
            return Utils.Client.rainbowDraw(2L, del);
        if (mode.is(ColourModes.ASTOLFO))
            return Utils.Client.astolfoColorsDraw(10, 14);
        if (mode.is(ColourModes.ASTOLFO2) || mode.is(ColourModes.ASTOLFO3))
            return Utils.Client.astolfoColorsDraw(10, del);
        return 0xFFFFFFFF;
    }

    private int getDeltaForMode(ModeValue mode) {
        return (mode.is(ColourModes.RAVEN2) || mode.is(ColourModes.ASTOLFO3)) ? 10 : 120;
    }

    public enum ColourModes {
        RAVEN, RAVEN2, ASTOLFO, ASTOLFO2, ASTOLFO3, KOPAMED
    }

    public enum PositionMode {
        UPLEFT(true, true),
        UPRIGHT(false, true),
        DOWNLEFT(true, false),
        DOWNRIGHT(false, false);

        private final boolean left;
        private final boolean top;

        PositionMode(boolean left, boolean top) {
            this.left = left;
            this.top = top;
        }

        public boolean isLeft() { return left; }
        public boolean isRight() { return !left; }
        public boolean isTop() { return top; }
        public boolean isBottom() { return !top; }
    }
}