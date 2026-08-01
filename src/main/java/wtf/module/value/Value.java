package wtf.module.value;

import java.util.function.BooleanSupplier;

import wtf.module.modules.Mod;

public abstract class Value {
    private final String name;
    private BooleanSupplier visible;
    private static final BooleanSupplier defaultVisible = () -> true;

    public Value(String name, Mod module, BooleanSupplier visible) {
        this.name = name;
        this.visible = visible != null ? visible : defaultVisible;
        
        if (module != null) {
            module.addSetting(this);
        }
    }

    public BooleanSupplier getVisible() {
        return visible;
    }

    public void setVisible(BooleanSupplier visible) {
        this.visible = visible != null ? visible : defaultVisible;
    }

    public String getName() {
        return name;
    }

    public boolean canDisplay() {
        return this.visible.getAsBoolean();
    }

    public abstract void resetToDefaults();

    public abstract String getSettingType();
}