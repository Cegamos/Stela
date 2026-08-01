package wtf.util.font;

import net.minecraft.client.renderer.GlStateManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class CustomFontRenderer extends CustomFont {
    protected final CustomFont.CharData[] boldChars = new CustomFont.CharData[65536];
    protected final CustomFont.CharData[] italicChars = new CustomFont.CharData[65536];
    protected final CustomFont.CharData[] boldItalicChars = new CustomFont.CharData[65536];
    protected final int[] colorCode = new int[32];
    protected final String colorcodeIdentifiers = "0123456789abcdefklmnor";

    public CustomFontRenderer(Font font, boolean antiAlias, boolean fractionalMetrics) {
        super(font, antiAlias, fractionalMetrics);
        setupMinecraftColorCodes();
        setupBoldItalic();
    }

    private void setupMinecraftColorCodes() {
        for (int i = 0; i < 32; ++i) {
            int base = (i >> 3 & 1) * 85;
            int red = (i >> 2 & 1) * 170 + base;
            int green = (i >> 1 & 1) * 170 + base;
            int blue = (i & 1) * 170 + base;

            if (i == 6) {
                red += 85;
            }

            if (i >= 16) {
                red /= 4;
                green /= 4;
                blue /= 4;
            }

            this.colorCode[i] = (red & 255) << 16 | (green & 255) << 8 | blue & 255;
        }
    }

    private void setupBoldItalic() {
        Font boldFont = font.deriveFont(Font.BOLD);
        Font italicFont = font.deriveFont(Font.ITALIC);
        Font boldItalicFont = font.deriveFont(Font.BOLD | Font.ITALIC);

        generateFontTexture(boldFont, this.antiAlias, this.fractionalMetrics, this.boldChars);
        generateFontTexture(italicFont, this.antiAlias, this.fractionalMetrics, this.italicChars);
        generateFontTexture(boldItalicFont, this.antiAlias, this.fractionalMetrics, this.boldItalicChars);
    }

    public float drawStringWithShadow(String text, double x, double y, int color) {
        float shadowWidth = drawString(text, x + 0.5D, y + 0.5D, color, true);
        return Math.max(shadowWidth, drawString(text, x, y, color, false));
    }

    public float drawString(String text, double x, double y, int color) {
        return drawString(text, x, y, color, false);
    }

    public float drawCenteredString(String text, double x, double y, int color) {
        return drawString(text, x - (getStringWidth(text) / 2.0F), y, color, false);
    }

    public float drawCenteredStringWithShadow(String text, double x, double y, int color) {
        return drawStringWithShadow(text, x - (getStringWidth(text) / 2.0F), y, color);
    }

    public float drawString(String text, double x, double y, int color, boolean shadow) {
        x -= 1.0D;

        if (text == null) {
            return 0.0F;
        }

        if (color == 553648127) {
            color = 16777215;
        }

        if ((color & -67108864) == 0) {
            color |= -16777216;
        }

        if (shadow) {
            color = (color & 16579836) >> 2 | color & -16777216;
        }

        CustomFont.CharData[] currentData = this.charData;
        float alpha = (float) (color >> 24 & 255) / 255.0F;
        boolean bold = false;
        boolean italic = false;
        boolean underline = false;
        boolean strikethrough = false;

        x *= 2.0D;
        y = (y - 3.0D) * 2.0D;

        GL11.glPushMatrix();
        GlStateManager.scale(0.5D, 0.5D, 0.5D);
        GlStateManager.enableBlend();
        GlStateManager.blendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GlStateManager.color((float) (color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F, alpha);
        GlStateManager.enableTexture2D();
        GlStateManager.bindTexture(this.tex.getGlTextureId());
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, this.tex.getGlTextureId());

        for (int i = 0; i < text.length(); ++i) {
            char character = text.charAt(i);

            if (character == '\u00a7') {
                int colorIndex = 21;
                try {
                    colorIndex = colorcodeIdentifiers.indexOf(text.charAt(i + 1));
                } catch (Exception ignored) {
                }

                if (colorIndex < 16) {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                    currentData = this.charData;

                    if (colorIndex < 0) {
                        colorIndex = 15;
                    }

                    if (shadow) {
                        colorIndex += 16;
                    }

                    int cCode = this.colorCode[colorIndex];
                    GlStateManager.color((float) (cCode >> 16 & 255) / 255.0F, (float) (cCode >> 8 & 255) / 255.0F, (float) (cCode & 255) / 255.0F, alpha);
                } else if (colorIndex == 17) {
                    bold = true;
                    if (italic) {
                        currentData = this.boldItalicChars;
                    } else {
                        currentData = this.boldChars;
                    }
                } else if (colorIndex == 18) {
                    strikethrough = true;
                } else if (colorIndex == 19) {
                    underline = true;
                } else if (colorIndex == 20) {
                    italic = true;
                    if (bold) {
                        currentData = this.boldItalicChars;
                    } else {
                        currentData = this.italicChars;
                    }
                } else {
                    bold = false;
                    italic = false;
                    underline = false;
                    strikethrough = false;
                    GlStateManager.color((float) (color >> 16 & 255) / 255.0F, (float) (color >> 8 & 255) / 255.0F, (float) (color & 255) / 255.0F, alpha);
                    GlStateManager.bindTexture(this.tex.getGlTextureId());
                    currentData = this.charData;
                }

                ++i;
            } else if (character < currentData.length && currentData[character] != null) {
                GL11.glBegin(GL11.GL_QUADS);
                drawChar(currentData, character, (float) x, (float) y);
                GL11.glEnd();

                if (strikethrough) {
                    drawLine(x, y + (double) (currentData[character].height / 2), x + (double) currentData[character].width - 8.0D, y + (double) (currentData[character].height / 2), 1.0F);
                }

                if (underline) {
                    drawLine(x, y + (double) currentData[character].height - 2.0D, x + (double) currentData[character].width - 8.0D, y + (double) currentData[character].height - 2.0D, 1.0F);
                }

                x += (double) (currentData[character].width - 8 + this.charOffset);
            }
        }

        GL11.glHint(GL11.GL_POLYGON_SMOOTH_HINT, GL11.GL_DONT_CARE);
        GL11.glPopMatrix();

        return (float) x / 2.0F;
    }

    private void drawLine(double x, double y, double x1, double y1, float width) {
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glLineWidth(width);
        GL11.glBegin(GL11.GL_LINES);
        GL11.glVertex2d(x, y);
        GL11.glVertex2d(x1, y1);
        GL11.glEnd();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public int getHeight() {
        return getFontHeight();
    }
}
