package wtf.util.font;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.geom.Rectangle2D;
import java.awt.image.BufferedImage;

import org.lwjgl.opengl.GL11;

import net.minecraft.client.renderer.texture.DynamicTexture;

public class CustomFont {
    protected final Font font;
    protected final boolean antiAlias;
    protected final boolean fractionalMetrics;
    protected final CharData[] charData = new CharData[65536];
    protected DynamicTexture tex;
    protected int fontHeight = -1;
    protected int charOffset = 0;

    public CustomFont(Font font, boolean antiAlias, boolean fractionalMetrics) {
        this.font = font;
        this.antiAlias = antiAlias;
        this.fractionalMetrics = fractionalMetrics;
        setupTextureAtlas();
    }

    protected void setupTextureAtlas() {
        BufferedImage img = generateFontTexture(this.font, this.antiAlias, this.fractionalMetrics, this.charData);
        try {
            this.tex = new DynamicTexture(img);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected BufferedImage generateFontTexture(Font font, boolean antiAlias, boolean fractionalMetrics, CharData[] chars) {
        int imgSize = 1024;
        BufferedImage bufferedImage = new BufferedImage(imgSize, imgSize, BufferedImage.TYPE_INT_ARGB);
        Graphics2D g = (Graphics2D) bufferedImage.getGraphics();
        g.setFont(font);

        g.setColor(new Color(255, 255, 255, 0));
        g.fillRect(0, 0, imgSize, imgSize);

        g.setColor(Color.WHITE);
        g.setRenderingHint(RenderingHints.KEY_FRACTIONALMETRICS, fractionalMetrics ? RenderingHints.VALUE_FRACTIONALMETRICS_ON : RenderingHints.VALUE_FRACTIONALMETRICS_OFF);
        g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, antiAlias ? RenderingHints.VALUE_TEXT_ANTIALIAS_ON : RenderingHints.VALUE_TEXT_ANTIALIAS_OFF);
        g.setRenderingHint(RenderingHints.KEY_ANTIALIASING, antiAlias ? RenderingHints.VALUE_ANTIALIAS_ON : RenderingHints.VALUE_ANTIALIAS_OFF);

        FontMetrics fontMetrics = g.getFontMetrics();

        int posX = 1;
        int posY = 1;
        int maxRowHeight = 0;

        for (int i = 0; i < chars.length; i++) {
            char c = (char) i;
            if (i < 32 || (i > 126 && i < 160 && i != 176) || (i > 255 && (i < 0xF000 || i > 0xF8FF))) {
                continue;
            }
            if (!font.canDisplay(c) && i >= 256) {
                continue;
            }

            Rectangle2D bounds = fontMetrics.getStringBounds(String.valueOf(c), g);
            int width = (int) Math.ceil(bounds.getWidth()) + 2;
            int height = (int) Math.ceil(bounds.getHeight()) + 2;

            if (height > this.fontHeight) {
                this.fontHeight = height;
            }

            if (posX + width >= imgSize) {
                posX = 1;
                posY += maxRowHeight + 1;
                maxRowHeight = 0;
            }

            if (posY + height >= imgSize) {
                break;
            }

            if (height > maxRowHeight) {
                maxRowHeight = height;
            }

            CharData charData = new CharData();
            charData.width = width;
            charData.height = height;
            charData.storedX = posX;
            charData.storedY = posY;

            g.drawString(String.valueOf(c), posX, posY + fontMetrics.getAscent());
            chars[i] = charData;
            posX += width + 2;
        }

        return bufferedImage;
    }

    public void drawChar(CharData[] chars, char c, float x, float y) throws ArrayIndexOutOfBoundsException {
        try {
            CharData charData = chars[c];
            if (charData == null) return;
            drawCharTile(charData, x, y);
        } catch (Exception ignored) {
        }
    }

    protected void drawCharTile(CharData charData, float x, float y) {
        float u = (float) charData.storedX / 1024.0F;
        float v = (float) charData.storedY / 1024.0F;
        float uWidth = (float) charData.width / 1024.0F;
        float vHeight = (float) charData.height / 1024.0F;

        GL11.glTexCoord2f(u, v);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u, v + vHeight);
        GL11.glVertex2f(x, y + charData.height);
        GL11.glTexCoord2f(u + uWidth, v + vHeight);
        GL11.glVertex2f(x + charData.width, y + charData.height);
        GL11.glTexCoord2f(u + uWidth, v);
        GL11.glVertex2f(x + charData.width, y);
    }

    public int getFontHeight() {
        return (this.fontHeight - 2) / 2;
    }

    public int getStringWidth(String text) {
        if (text == null) return 0;
        int width = 0;
        char[] chars = text.toCharArray();
        for (int i = 0; i < chars.length; i++) {
            char c = chars[i];
            if (c == '\u00a7' && i + 1 < chars.length) {
                i++;
                continue;
            }
            if (c < this.charData.length && this.charData[c] != null) {
                width += this.charData[c].width - 2 + this.charOffset;
            }
        }
        return width / 2;
    }

    public Font getFont() {
        return this.font;
    }

    public boolean isAntiAlias() {
        return this.antiAlias;
    }

    public boolean isFractionalMetrics() {
        return this.fractionalMetrics;
    }

    public static class CharData {
        public int width;
        public int height;
        public int storedX;
        public int storedY;
    }
}
