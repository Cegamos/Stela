package keystrokesmod.client.module.modules.client;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
import keystrokesmod.client.clickgui.raven.ClickGui;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.DragEvent;
import keystrokesmod.client.event.impl.DrawEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.font.FontRenderer;
import keystrokesmod.client.util.font.Fonts;
import net.minecraft.client.gui.GuiIngameMenu;
import net.minecraft.client.gui.inventory.GuiContainer;

@ModuleInfo(name = "HUD", category = Category.Client)
public class HUD extends Mod {

    public final ComboSetting mode = new ComboSetting("Mode", this, ColourModes.ASTOLFO2, ColourModes.values());
    private final ComboSetting fontMode = new ComboSetting("Font Mode", this, "Rubik", "Rubik", "Drip", "Semi bold");
    public final TickSetting alphabeticalSort = new TickSetting("Alphabetical sort", this, false);

    private final DescriptionSetting desc = new DescriptionSetting("Hide Category", this);
    public final TickSetting hideClient = new TickSetting("Hide Client", this, false);
    public final TickSetting hideCombat = new TickSetting("Hide Combat", this, false);
    public final TickSetting hideMovement = new TickSetting("Hide Movement", this, false);
    public final TickSetting hideOther = new TickSetting("Hide Other", this, false);
    public final TickSetting hidePlayer = new TickSetting("Hide Player", this, false);
    public final TickSetting hideRender = new TickSetting("Hide Render", this, false);
    public final TickSetting hideWorld = new TickSetting("Hide World", this, false);

    public static final AtomicInteger hudX = new AtomicInteger(5);
    public static final AtomicInteger hudY = new AtomicInteger(70);
    public static final AtomicReference<PositionMode> positionMode = new AtomicReference<>(PositionMode.UPLEFT);

    private static final AtomicBoolean draggingModuleList = new AtomicBoolean(false);
    private static final AtomicReference<Float> dragOffsetX = new AtomicReference<>(0f);
    private static final AtomicReference<Float> dragOffsetY = new AtomicReference<>(0f);

    @Override
    public void onEnable() {
        Raven.moduleManager.sort();
    }

    @Override
    public void guiButtonToggled(final TickSetting setting) {
        if (setting == alphabeticalSort) Raven.moduleManager.sort();
    }

    @EventLink
    public final Listener<DrawEvent> onDraw = event -> {
        if (!Utils.Player.isPlayerInGame()) return;
        if (mc.gameSettings.showDebugInfo || mc.currentScreen instanceof ClickGui
                || mc.currentScreen instanceof GuiContainer || mc.currentScreen instanceof GuiIngameMenu)
            return;

        final int margin = 2;
        int y = hudY.get();
        int del = 0;

        if (!alphabeticalSort.isToggled()) {
            if (positionMode.get().isTop()) Raven.moduleManager.sortShortLong();
            else Raven.moduleManager.sortLongShort();
        }

        List<Mod> modules = Raven.moduleManager.getModules();
        if (modules.isEmpty()) return;

        final int textBoxWidth = Raven.moduleManager.getLongestActiveModule();
        final int textBoxHeight = Raven.moduleManager.getBoxHeight(margin);
        final FontRenderer font = getFont();

        int correctedX = Math.max(hudX.get(), margin);
        int correctedY = Math.max(hudY.get(), margin);
        correctedX = Math.min(correctedX, mc.displayWidth / 2 - textBoxWidth - margin);
        correctedY = Math.min(correctedY, mc.displayHeight / 2 - textBoxHeight);

        hudX.set(correctedX);
        hudY.set(correctedY);

        final boolean rightAligned = positionMode.get().isRight();

        for (Mod m : modules) {
            if (!m.isEnabled() || m == this || !m.shouldDisplay(this)) continue;

            float drawX = rightAligned
                    ? hudX.get() + (float) (textBoxWidth - font.getStringWidth(m.getName()))
                    : hudX.get();

            int color = getColorForMode(mode, del);
            font.drawString(m.getName(), drawX, (float) y, color);

            y += font.getHeight() + margin;
            del -= getDeltaForMode(mode);
        }
    };

    @EventLink
    public final Listener<DragEvent> onDrag = event -> {
        final FontRenderer font = getFont();
        final int mouseX = event.mouseX;
        final int mouseY = event.mouseY;

        List<Mod> activeModules = Raven.moduleManager.getModules().stream()
                .filter(m -> m.isEnabled() && m != Raven.moduleManager.getModuleByClazz(HUD.class))
                .collect(Collectors.toList());

        if (activeModules.isEmpty()) return;

        float maxWidth = (float) activeModules.stream()
                .mapToDouble(m -> font.getStringWidth(m.getName()) + 4)
                .max()
                .orElse(0);

        float height = activeModules.size() * (font.getHeight() + 2);
        boolean mouseDown = Mouse.isButtonDown(0);

        if (mouseDown) {
            boolean hovering = mouseX >= hudX.get() && mouseX <= hudX.get() + maxWidth
                    && mouseY >= hudY.get() && mouseY <= hudY.get() + height;

            if (hovering && !draggingModuleList.get()) {
                draggingModuleList.set(true);
                dragOffsetX.set(mouseX - hudX.get() * 1f);
                dragOffsetY.set(mouseY - hudY.get() * 1f);
            }

            if (draggingModuleList.get()) {
                hudX.set((int) (mouseX - dragOffsetX.get()));
                hudY.set((int) (mouseY - dragOffsetY.get()));
            }
        } else {
            draggingModuleList.set(false);
        }
    };

    public FontRenderer getFont() {
        switch (fontMode.getMode()) {
            case "Drip":
                return Fonts.DRIP.get(16);
            case "Semi bold":
                return Fonts.SEMIBOLD.get(16);
            default:
                return Fonts.RUBIK.get(16);
        }
    }

    private int getColorForMode(ComboSetting mode, int del) {
        if (mode.is(ColourModes.RAVEN) || mode.is(ColourModes.RAVEN2))
            return Utils.Client.rainbowDraw(2L, del);
        if (mode.is(ColourModes.ASTOLFO))
            return Utils.Client.astolfoColorsDraw(10, 14);
        if (mode.is(ColourModes.ASTOLFO2) || mode.is(ColourModes.ASTOLFO3))
            return Utils.Client.astolfoColorsDraw(10, del);
        return 0xFFFFFFFF;
    }

    private int getDeltaForMode(ComboSetting mode) {
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
