package keystrokesmod.keystroke;

import java.awt.Color;
import java.io.IOException;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.DrawEvent;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;

public class KeyStrokeRenderer {
    private static final int[] a = new int[] { 16777215, 16711680, 65280, 255, 16776960, 11141290 };
    private Minecraft mc = null;
    private final KeyStrokeKeyRenderer[] b;
    private final KeyStrokeMouse[] c;
    
    public KeyStrokeRenderer() {
        this.mc = Minecraft.getMinecraft();
        this.b = new KeyStrokeKeyRenderer[4];
        this.c = new KeyStrokeMouse[2];
    }

    private boolean checkInit() {
        if (this.mc == null || this.mc.gameSettings == null) return false;
        if (this.b[0] == null) {
            this.b[0] = new KeyStrokeKeyRenderer(this.mc.gameSettings.keyBindForward, 26, 2);
            this.b[1] = new KeyStrokeKeyRenderer(this.mc.gameSettings.keyBindBack, 26, 26);
            this.b[2] = new KeyStrokeKeyRenderer(this.mc.gameSettings.keyBindLeft, 2, 26);
            this.b[3] = new KeyStrokeKeyRenderer(this.mc.gameSettings.keyBindRight, 50, 26);
            this.c[0] = new KeyStrokeMouse(0, 2, 50);
            this.c[1] = new KeyStrokeMouse(1, 38, 50);
        }
        return true;
    }
    
    @EventLink
    public final Listener<DrawEvent> onDraw = e -> {
        if (!checkInit()) return;
        if (this.mc.currentScreen != null) {
            if (this.mc.currentScreen instanceof KeyStrokeConfigGui) {
                try {
                    this.mc.currentScreen.handleInput();
                }
                catch (IOException ex) {}
            }
        }
        else if (this.mc.inGameHasFocus && !this.mc.gameSettings.showDebugInfo) {
            this.renderKeystrokes();
        }
    };
    
    public void renderKeystrokes() {
        if (KeyStroke.enabled) {
            int x = KeyStroke.x;
            int y = KeyStroke.y;
            final int g = this.getColor(KeyStroke.currentColorNumber);
            final boolean h = KeyStroke.showMouseButtons;
            final ScaledResolution res = new ScaledResolution(this.mc);
            final int width = 74;
            final int height = h ? 74 : 50;
            if (x < 0) {
                KeyStroke.x = 0;
                x = 0;
            }
            else if (x > res.getScaledWidth() - width) {
                KeyStroke.x = res.getScaledWidth() - width;
                x = res.getScaledWidth() - width;
            }
            if (y < 0) {
                KeyStroke.y = 0;
                y = 0;
            }
            else if (y > res.getScaledHeight() - height) {
                KeyStroke.y = res.getScaledHeight() - height;
                y = res.getScaledHeight() - height;
            }
            this.b[0].renderKey(x + 26, y + 2, g);
            this.b[1].renderKey(x + 26, y + 26, g);
            this.b[2].renderKey(x + 2, y + 26, g);
            this.b[3].renderKey(x + 50, y + 26, g);
            if (h) {
                this.c[0].renderMouse(x + 2, y + 50, g);
                this.c[1].renderMouse(x + 38, y + 50, g);
            }
        }
    }
    
    private int getColor(final int index) {
        return (index == 6) ? Color.HSBtoRGB(System.currentTimeMillis() % 1000L / 1000.0f, 0.8f, 0.8f) : KeyStrokeRenderer.a[index];
    }
}
