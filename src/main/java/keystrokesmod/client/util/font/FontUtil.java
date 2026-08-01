package keystrokesmod.client.util.font;

import java.awt.*;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

public class FontUtil {
    private static final Map<String, Font> fontCache = new HashMap<>();

    // FontAwesome Instances (Solid - fa-solid-900.ttf)
    public static CustomFontRenderer faSolid14;
    public static CustomFontRenderer faSolid16;
    public static CustomFontRenderer faSolid18;
    public static CustomFontRenderer faSolid20;
    public static CustomFontRenderer faSolid24;
    public static CustomFontRenderer faSolid30;

    // FontAwesome Instances (Regular - fa-regular-400.ttf)
    public static CustomFontRenderer faRegular14;
    public static CustomFontRenderer faRegular16;
    public static CustomFontRenderer faRegular18;
    public static CustomFontRenderer faRegular20;
    public static CustomFontRenderer faRegular24;

    // FontAwesome Instances (Light - fa-light-300.ttf)
    public static CustomFontRenderer faLight14;
    public static CustomFontRenderer faLight16;
    public static CustomFontRenderer faLight18;
    public static CustomFontRenderer faLight20;
    public static CustomFontRenderer faLight24;

    // Standard Custom UI Fonts
    public static CustomFontRenderer regular14;
    public static CustomFontRenderer regular16;
    public static CustomFontRenderer regular18;
    public static CustomFontRenderer regular20;
    public static CustomFontRenderer regular24;

    public static CustomFontRenderer bold14;
    public static CustomFontRenderer bold16;
    public static CustomFontRenderer bold18;
    public static CustomFontRenderer bold20;
    public static CustomFontRenderer bold24;

    public static void init() {
        // Initialize FontAwesome Solid
        faSolid14 = getFontRenderer("fa-solid-900", 14.0F);
        faSolid16 = getFontRenderer("fa-solid-900", 16.0F);
        faSolid18 = getFontRenderer("fa-solid-900", 18.0F);
        faSolid20 = getFontRenderer("fa-solid-900", 20.0F);
        faSolid24 = getFontRenderer("fa-solid-900", 24.0F);
        faSolid30 = getFontRenderer("fa-solid-900", 30.0F);

        // Initialize FontAwesome Regular
        faRegular14 = getFontRenderer("fa-regular-400", 14.0F);
        faRegular16 = getFontRenderer("fa-regular-400", 16.0F);
        faRegular18 = getFontRenderer("fa-regular-400", 18.0F);
        faRegular20 = getFontRenderer("fa-regular-400", 20.0F);
        faRegular24 = getFontRenderer("fa-regular-400", 24.0F);

        // Initialize FontAwesome Light
        faLight14 = getFontRenderer("fa-light-300", 14.0F);
        faLight16 = getFontRenderer("fa-light-300", 16.0F);
        faLight18 = getFontRenderer("fa-light-300", 18.0F);
        faLight20 = getFontRenderer("fa-light-300", 20.0F);
        faLight24 = getFontRenderer("fa-light-300", 24.0F);

        // Initialize Standard Custom UI Fonts (loads custom font or fallbacks gracefully)
        regular14 = getFontRenderer("regular", 14.0F);
        regular16 = getFontRenderer("regular", 16.0F);
        regular18 = getFontRenderer("regular", 18.0F);
        regular20 = getFontRenderer("regular", 20.0F);
        regular24 = getFontRenderer("regular", 24.0F);

        bold14 = getFontRenderer("bold", 14.0F);
        bold16 = getFontRenderer("bold", 16.0F);
        bold18 = getFontRenderer("bold", 18.0F);
        bold20 = getFontRenderer("bold", 20.0F);
        bold24 = getFontRenderer("bold", 24.0F);
    }

    public static CustomFontRenderer getFontRenderer(String name, float size) {
        return getFontRenderer(name, size, true, true);
    }

    public static CustomFontRenderer getFontRenderer(String name, float size, boolean antiAlias, boolean fractionalMetrics) {
        Font font = getRawFont(name, size);
        return new CustomFontRenderer(font, antiAlias, fractionalMetrics);
    }

    public static Font getRawFont(String name, float size) {
        String key = name + "_" + size;
        if (fontCache.containsKey(key)) {
            return fontCache.get(key);
        }

        Font font;
        try {
            String resourcePath = name.endsWith(".ttf") ? "/" + name : "/" + name + ".ttf";
            InputStream is = FontUtil.class.getResourceAsStream(resourcePath);
            if (is == null) {
                is = FontUtil.class.getResourceAsStream("/assets/minecraft/" + name + ".ttf");
            }
            if (is != null) {
                font = Font.createFont(Font.TRUETYPE_FONT, is).deriveFont(size);
            } else {
                font = new Font("SansSerif", Font.PLAIN, (int) size);
            }
        } catch (Exception e) {
            e.printStackTrace();
            font = new Font("SansSerif", Font.PLAIN, (int) size);
        }

        fontCache.put(key, font);
        return font;
    }
}
