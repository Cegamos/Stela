package keystrokesmod.client.module.modules;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.Value;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Wrapper;

public abstract class Mod extends Wrapper {
    protected final List<Value> settings = new ArrayList<>();
    
    private final ModuleInfo moduleInfo;
    private final String moduleName;
    private final Category moduleCategory;
    
    protected boolean enabled;
    protected int keycode;
    private boolean isToggled = false;
    public boolean ignoreOnSave;

    protected Mod() {
        this.moduleInfo = this.getClass().getAnnotation(ModuleInfo.class);
        Objects.requireNonNull(moduleInfo, "ModuleInfo annotation is missing on " + getClass().getName());
        this.moduleName = moduleInfo.name();
        this.moduleCategory = moduleInfo.category();
        this.keycode = moduleInfo.key();
        this.enabled = moduleInfo.enabled();
    }

    @SuppressWarnings("unchecked")
    protected <E extends Mod> E withEnabled(boolean state) {
        this.enabled = state;
        try {
            this.setToggled(state);
        } catch (Exception ignored) {}
        return (E) this;
    }

    public void keybind() {
        if (this.keycode != 0 && this.canBeEnabled()) {
            if (!this.isToggled && Keyboard.isKeyDown(this.keycode)) {
                this.toggle();
                this.isToggled = true;
            } else if (!Keyboard.isKeyDown(this.keycode)) {
                this.isToggled = false;
            }
        }
    }

    public void setToggled(boolean enabled) {
        if (enabled) {
            this.enable();
        } else {
            this.disable();
        }
    }

    public void toggle() {
        this.setToggled(!this.enabled);
    }

    public void enable() {
        if (!this.enabled) {
            this.enabled = true;
            this.onEnable();
            EventBus.INSTANCE.register(this);
        }
    }

    public void disable() {
        if (this.enabled) {
            this.enabled = false;
            this.onDisable();
            EventBus.INSTANCE.unregister(this);
        }
    }

    public boolean canBeEnabled() {
        return true;
    }

    public boolean isEnabled() {
        return this.enabled;
    }

    public String getName() {
        return this.moduleName;
    }

    public Category moduleCategory() {
        return this.moduleCategory;
    }

    public Category getCategory() {
        return this.moduleCategory;
    }

    public List<Value> getSettings() {
        return this.settings;
    }

    public void addSetting(Value... newSettings) {
        for (int i = 0; i < newSettings.length; i++) {
            Value setting = newSettings[i];
            if (setting != null) {
                this.settings.add(setting);
            }
        }
    }

    public Value getSettingByName(String name) {
        if (name == null) return null;
        
        for (int i = 0; i < this.settings.size(); i++) {
            Value val = this.settings.get(i);
            if (val.getName().equalsIgnoreCase(name)) {
                return val;
            }
        }
        return null;
    }

    public void resetToDefaults() {
        this.keycode = moduleInfo.key();
        this.setToggled(moduleInfo.enabled());
        for (int i = 0; i < this.settings.size(); i++) {
            this.settings.get(i).resetToDefaults();
        }
    }

    public String getBindAsString() {
        return (this.keycode == 0) ? "None" : Keyboard.getKeyName(this.keycode);
    }

    public void clearBinds() {
        this.keycode = 0;
    }

    public int getKeycode() {
        return this.keycode;
    }

    public void setbind(int keybind) {
        this.keycode = keybind;
    }

    public void setKeycode(int keycode) {
        this.keycode = keycode;
    }

    public void onEnable() {}

    public void onDisable() {}

    public void update() {}

    public void guiUpdate() {}

    public void guiButtonToggled(BooleanValue setting) {}
}