package keystrokesmod.client.util.render;

import java.awt.Color;

public class ColorUtil {

    public static Color interpolateColorC(Color color1, Color color2, float amount) {
        amount = Math.min(1.0F, Math.max(0.0F, amount));
        return new Color(
                (int) lerp(color1.getRed(), color2.getRed(), amount),
                (int) lerp(color1.getGreen(), color2.getGreen(), amount),
                (int) lerp(color1.getBlue(), color2.getBlue(), amount),
                (int) lerp(color1.getAlpha(), color2.getAlpha(), amount)
        );
    }

    public static int interpolateColor(int color1, int color2, float amount) {
        return interpolateColorC(new Color(color1, true), new Color(color2, true), amount).getRGB();
    }

    public static float lerp(float start, float stop, float amount) {
        return start + (stop - start) * amount;
    }

    public static Color getRainbow(float seconds, float saturation, float brightness) {
        float hue = (System.currentTimeMillis() % (int) (seconds * 1000.0F)) / (seconds * 1000.0F);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static Color getRainbow(float seconds, float saturation, float brightness, long index) {
        float hue = ((System.currentTimeMillis() + index) % (int) (seconds * 1000.0F)) / (seconds * 1000.0F);
        return Color.getHSBColor(hue, saturation, brightness);
    }

    public static Color reAlpha(Color color, int alpha) {
        alpha = Math.max(0, Math.min(255, alpha));
        return new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
    }

    public static int reAlpha(int color, int alpha) {
        return reAlpha(new Color(color, true), alpha).getRGB();
    }
}
