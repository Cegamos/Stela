package keystrokesmod.client.module.value;

import java.util.Optional;
import java.util.function.Supplier;

import keystrokesmod.client.module.modules.Mod;

public abstract class Value {
    private final String name;
    private Supplier<Boolean> visible;

    public Value(String name, Mod module, Supplier<Boolean> visible) {
        this.name = name;
        this.visible = visible != null ? visible : () -> true;
        Optional.ofNullable(module).ifPresent(m -> m.addSetting(this));
    }

    public Supplier<Boolean> getVisible() {
        return visible;
    }

    public void setVisible(Supplier<Boolean> visible) {
        this.visible = visible != null ? visible : () -> true;
    }

    public String getName() {
        return name;
    }

    public Boolean canDisplay() {
        return this.visible.get();
    }

    public abstract void resetToDefaults();

    public abstract String getSettingType();
}
