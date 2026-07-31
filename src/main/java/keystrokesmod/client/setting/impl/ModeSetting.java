package keystrokesmod.client.setting.impl;

import keystrokesmod.client.setting.Setting;

import java.util.Arrays;
import java.util.List;
import java.util.function.Supplier;

public class ModeSetting extends Setting<String> {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String defaultMode, String... modes) {
        super(name, defaultMode);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) this.index = 0;
    }

    public ModeSetting(String name, String defaultMode, Supplier<Boolean> visibility, String... modes) {
        super(name, defaultMode, visibility);
        this.modes = Arrays.asList(modes);
        this.index = this.modes.indexOf(defaultMode);
        if (this.index == -1) this.index = 0;
    }

    public void cycle() {
        if (modes.isEmpty()) return;
        index = (index + 1) % modes.size();
        setValue(modes.get(index));
    }

    public List<String> getModes() {
        return modes;
    }

    public int getIndex() {
        return index;
    }
}
