package keystrokesmod.client.setting.impl;

import keystrokesmod.client.setting.Setting;

import java.util.function.Supplier;

public class KeybindSetting extends Setting<Integer> {

    public KeybindSetting(String name, int defaultKey) {
        super(name, defaultKey);
    }

    public KeybindSetting(String name, int defaultKey, Supplier<Boolean> visibility) {
        super(name, defaultKey, visibility);
    }
}
