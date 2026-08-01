package wtf.util.font;

import java.awt.Font;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import org.lwjgl.opengl.Display;

public class FontUtil {
    private static final Map<String, Font> fontCache = new HashMap<>();
    private static boolean initialized = false;

    public static CustomFontRenderer faSolid14;
    
    public static synchronized void checkInit() {
        if (!initialized && Display.isCreated()) {
            initialized = true;
            init();
        }
    }

    public static void init() {
        faSolid14 = getFontRenderer("fa-solid-900", 14.0F);
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