package keystrokesmod.client.setting.impl;

import keystrokesmod.client.setting.Setting;

import java.util.function.Supplier;

public class BooleanSetting extends Setting<Boolean> {

    public BooleanSetting(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public BooleanSetting(String name, boolean defaultValue, Supplier<Boolean> visibility) {
        super(name, defaultValue, visibility);
    }

    public void toggle() {
        setValue(!getValue());
    }
}
