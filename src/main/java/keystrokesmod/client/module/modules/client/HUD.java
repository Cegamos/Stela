package keystrokesmod.client.module.modules.client;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
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
import keystrokesmod.client.util.Utils;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.ScaledResolution;

@ModuleInfo(name = "HUD", category = Category.Client)
public class HUD extends Mod {

    public final ModeValue mode = new ModeValue("Mode", this, ColourModes.ASTOLFO2, ColourModes.values());
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
    
    private int cachedMaxWidth = 0;
    private int cachedTotalHeight = 0;

    private ScaledResolution cachedSR;
    private int lastDisplayWidth = -1, lastDisplayHeight = -1, lastGuiScale = -1;

    private final Comparator<Mod> sortLongShort = (m1, m2) -> getFont().getStringWidth(m2.getName()) - getFont().getStringWidth(m1.getName());
    private final Comparator<Mod> sortShortLong = (m1, m2) -> getFont().getStringWidth(m1.getName()) - getFont().getStringWidth(m2.getName());
    private final Comparator<Mod> sortAlphabetical = Comparator.comparing(Mod::getName);

    @Override
    public void onEnable() {
        super.onEnable();
        if (mc != null && mc.fontRendererObj != null) {
            Raven.moduleManager.sort();
        }
    }

    @Override
    public void guiButtonToggled(final BooleanValue setting) {
        super.guiButtonToggled(setting);
        if (setting == alphabeticalSort && mc != null && mc.fontRendererObj != null) {
            Raven.moduleManager.sort();
        }
    }

    @EventLink
    private Listener<PostRenderTickEvent> onDraw = event -> {
        if (checkGame() || gameSetting().showDebugInfo || currentScreen() instanceof ClickGui) return;

        final FontRenderer font = getFont();
        if (font == null) return;

        renderList.clear();
        for (Mod m : Raven.moduleManager.getModules()) {
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

        final int margin = 2;
        int maxWidth = 0;

        for (int i = 0; i < renderList.size(); i++) {
            int width = font.getStringWidth(renderList.get(i).getName());
            if (width > maxWidth) maxWidth = width;
        }

        int totalHeight = renderList.size() * (font.FONT_HEIGHT + margin);
        
        cachedMaxWidth = maxWidth;
        cachedTotalHeight = totalHeight;

        ScaledResolution sr = getScaledResolution();
        int correctedX = Math.max(hudX, margin);
        int correctedY = Math.max(hudY, margin);
        correctedX = Math.min(correctedX, sr.getScaledWidth() - maxWidth - margin);
        correctedY = Math.min(correctedY, sr.getScaledHeight() - totalHeight - margin);

        hudX = correctedX;
        hudY = correctedY;

        final boolean rightAligned = positionMode.isRight();
        int y = hudY;
        int del = 0;

        for (int i = 0; i < renderList.size(); i++) {
            Mod m = renderList.get(i);
            float drawX = rightAligned
                    ? hudX + (float) (maxWidth - font.getStringWidth(m.getName()))
                    : hudX;

            int color = getColorForMode(mode, del);
            font.drawString(m.getName(), drawX, (float) y, color, true);

            y += font.FONT_HEIGHT + margin;
            del -= getDeltaForMode(mode);
        }
    };

    @EventLink
    private Listener<DragEvent> onDrag = event -> {
        if (cachedMaxWidth == 0 || cachedTotalHeight == 0) return;

        final int mouseX = event.mouseX;
        final int mouseY = event.mouseY;
        boolean mouseDown = Mouse.isButtonDown(0);

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

    private ScaledResolution getScaledResolution() {
        int scale = mc.gameSettings.guiScale;
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

    public FontRenderer getFont() {
        return mc.fontRendererObj;
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