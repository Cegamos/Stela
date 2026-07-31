package keystrokesmod.client.util.font;

import java.awt.FontFormatException;
import java.io.IOException;
import java.util.HashMap;
import java.util.function.Supplier;

import net.minecraft.client.Minecraft;

public enum Fonts {
    RUBIK("rubik", "ttf"),
    SEMIBOLD("semibold", "ttf"),
    DRIP("drip", "otf");

    private final Supplier<FontRenderer> get;
    private FontRenderer font;
    private final String name;
    private final String extension;
    private final HashMap<Integer, FontRenderer> sizes = new HashMap<>();

    Fonts(String name, String extension) {
        this.name = name;
        this.extension = extension;
        this.get = null;
    }

    Fonts(String name, Supplier<FontRenderer> get) {
        this.name = name;
        this.extension = "";
        this.get = get;
        this.font = get.get();
    }

    public FontRenderer get(int size) {
        return get(size, Weight.NONE);
    }

    public FontRenderer get() {
        return get(0, Weight.NONE);
    }

    public FontRenderer get(int size, Weight weight) {
        if (get != null && font == null) {
            font = get.get();
            return font;
        }

        int key = generateKey(size, weight);

        if (!sizes.containsKey(key)) {
            java.awt.Font awtFont = null;

            String[] aliases = weight.getAliases().isEmpty() ? new String[]{""} : weight.getAliases().split(",");

            for (String alias : aliases) {
                String baseName = alias.isEmpty() ? name : String.format(name, alias);
                String location = String.format("%s.%s", baseName, extension);

                System.out.println("Intentando cargar fuente: " + location);
                awtFont = getResource(location, size);

                if (awtFont != null) break;
            }

            if (awtFont != null) {
                sizes.put(key, new FontRenderer(awtFont));
            } else {
                System.err.println("No se pudo cargar la fuente para: " + name + ", tamaño: " + size);
            }
        }

        return sizes.get(key);
    }

    private int generateKey(int size, Weight weight) {
        return 31 * size + weight.getNum();
    }

    public java.awt.Font getResource(final String resource, final int size) {
        try {
            return java.awt.Font.createFont(
                java.awt.Font.TRUETYPE_FONT,
                Minecraft.getMinecraft().getResourceManager().getResource(new net.minecraft.util.ResourceLocation("keystrokes", resource)).getInputStream()
            ).deriveFont((float) size);
        } catch (final FontFormatException | IOException e) {
            System.err.println("Error cargando fuente: " + resource);
            e.printStackTrace();
            return null;
        }
    }

    public enum Weight {
        NONE(0, ""),
        LIGHT(1, "Light"),
        BLACK(2, "Black"),
        BOLD(3, "Bold");

        private final int num;
        private final String aliases;

        Weight(int num, String aliases) {
            this.num = num;
            this.aliases = aliases;
        }

        public int getNum() {
            return num;
        }

        public String getAliases() {
            return aliases;
        }
    }
}