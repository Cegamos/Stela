package wtf.module.value.impl;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.function.BooleanSupplier;

import wtf.module.modules.Mod;
import wtf.module.value.Value;

public class ModeValue extends Value {
    private int index;
    private final List<String> list;

    public ModeValue(String name, Mod module, BooleanSupplier visible, String current, String... modes) {
        super(name, module, visible);
        this.list = Collections.unmodifiableList(Arrays.asList(modes));
        setMode(current);
    }

    public ModeValue(String name, Mod module, String current, String... modes) {
        this(name, module, null, current, modes);
    }

    public ModeValue(String name, Mod module, Enum<?> current, Enum<?>... enumModes) {
        this(name, module, null, current, enumModes);
    }

    public ModeValue(String name, Mod module, BooleanSupplier visible, Enum<?> current, Enum<?>... enumModes) {
        super(name, module, visible);
       
        String[] stringModes = new String[enumModes.length];
        for (int i = 0; i < enumModes.length; i++) {
            stringModes[i] = enumModes[i].name();
        }
        
        this.list = Collections.unmodifiableList(Arrays.asList(stringModes));
        setMode(current.name());
    }

    public String getMode() {
        if (index >= list.size() || index < 0) index = 0;
        return list.get(index);
    }

    public void setMode(String mode) {
        this.index = list.indexOf(mode);
        if (this.index == -1) this.index = 0;
    }

    public <T extends Enum<T>> void setMode(T enumValue) {
        setMode(enumValue.name());
    }

    public boolean is(String mode) {
        return getMode().equalsIgnoreCase(mode);
    }

    public <T extends Enum<T>> boolean is(T enumValue) {
        return getMode().equalsIgnoreCase(enumValue.name());
    }

    public void increment() {
        index = (index + 1) % list.size();
    }

    public void decrement() {
        index = (index - 1 + list.size()) % list.size();
    }

    @Override
    public void resetToDefaults() {
        this.index = 0;
    }

    @Override
    public String getSettingType() {
        return "mode";
    }

    public List<String> getModes() {
        return list;
    }
}