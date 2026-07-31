package keystrokesmod.keystroke;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;

import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import keystrokesmod.client.util.input.MouseManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;

public class KeyStrokeMouse {
    private static final String[] a;
    private final Minecraft b;
    private final int c;
    private final int d;
    private final int e;
    private final List<Long> f;
    private boolean g;
    private long h;
    
    public KeyStrokeMouse(final int k, final int l, final int m) {
        this.b = Minecraft.getMinecraft();
        this.f = new ArrayList<Long>();
        this.g = true;
        this.h = 0L;
        this.c = k;
        this.d = l;
        this.e = m;
    }
    
    public void renderMouse(final int o, final int p, final int color) {
        final boolean r = Mouse.isButtonDown(this.c);
        final String s = KeyStrokeMouse.a[this.c];
        if (r != this.g) {
            this.g = r;
            this.h = System.currentTimeMillis();
            if (r) {
                this.f.add(this.h);
            }
        }
        double j = 1.0;
        int i = 255;
        if (r) {
            i = Math.min(255, (int)(2L * (System.currentTimeMillis() - this.h)));
            j = Math.max(0.0, 1.0 - (System.currentTimeMillis() - this.h) / 20.0);
        }
        else {
            i = Math.max(0, 255 - (int)(2L * (System.currentTimeMillis() - this.h)));
            j = Math.min(1.0, (System.currentTimeMillis() - this.h) / 20.0);
        }
        final int t = color >> 16 & 0xFF;
        final int u = color >> 8 & 0xFF;
        final int v = color & 0xFF;
        final int c = new Color(t, u, v).getRGB();
        Gui.drawRect(o + this.d, p + this.e, o + this.d + 34, p + this.e + 22, 2013265920 + (i << 16) + (i << 8) + i);
        if (KeyStroke.outline) {
            Gui.drawRect(o + this.d, p + this.e, o + this.d + 34, p + this.e + 1, c);
            Gui.drawRect(o + this.d, p + this.e + 21, o + this.d + 34, p + this.e + 22, c);
            Gui.drawRect(o + this.d, p + this.e, o + this.d + 1, p + this.e + 22, c);
            Gui.drawRect(o + this.d + 33, p + this.e, o + this.d + 34, p + this.e + 22, c);
        }
        this.b.fontRendererObj.drawString(s, o + this.d + 8, p + this.e + 4, -16777216 + ((int)(t * j) << 16) + ((int)(u * j) << 8) + (int)(v * j));
        final String w = MouseManager.getLeftClickCounter() + " CPS";
        final String x = MouseManager.getRightClickCounter() + " CPS";
        final int y = this.b.fontRendererObj.getStringWidth(w);
        final int z = this.b.fontRendererObj.getStringWidth(x);
        final boolean a2 = this.c == 0;
        final int b2 = a2 ? y : z;
        GL11.glScalef(0.5f, 0.5f, 0.5f);
        this.b.fontRendererObj.drawString(a2 ? w : x, (o + this.d + 17) * 2 - b2 / 2, (p + this.e + 14) * 2, -16777216 + ((int)(255.0 * j) << 16) + ((int)(255.0 * j) << 8) + (int)(255.0 * j));
        GL11.glScalef(2.0f, 2.0f, 2.0f);
    }
    
    static {
        a = new String[] { "LMB", "RMB" };
    }
}
