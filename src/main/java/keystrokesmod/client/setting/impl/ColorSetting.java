package keystrokesmod.client.setting.impl;

import keystrokesmod.client.setting.Setting;

import java.awt.Color;
import java.util.function.Supplier;

public class ColorSetting extends Setting<Integer> {
    private boolean rainbow;

    public ColorSetting(String name, int defaultColor) {
        super(name, defaultColor);
        this.rainbow = false;
    }

    public ColorSetting(String name, int defaultColor, boolean rainbow) {
        super(name, defaultColor);
        this.rainbow = rainbow;
    }

    public ColorSetting(String name, int defaultColor, Supplier<Boolean> visibility) {
        super(name, defaultColor, visibility);
        this.rainbow = false;
    }

    public boolean isRainbow() {
        return rainbow;
    }

    public void setRainbow(boolean rainbow) {
        this.rainbow = rainbow;
    }

    public Color getColor() {
        return new Color(getValue(), true);
    }
}
