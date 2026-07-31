package keystrokesmod.client.module.modules;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

import org.lwjgl.input.Keyboard;

import com.google.gson.JsonObject;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.client.HUD;
import keystrokesmod.client.module.value.Value;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Wrapper;

public class Mod extends Wrapper {
    protected final ArrayList<Value> settings = new ArrayList<>();
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

    protected <E extends Mod> E withEnabled(boolean state) {
        this.enabled = state;
        try {
            this.setToggled(state);
        } catch (Exception ignored) {}
        return (E) this;
    }

    public JsonObject getConfigAsJson() {
        JsonObject settingsJson = new JsonObject();
        for (Value setting : this.settings) {
            settingsJson.add(setting.getName(), setting.getConfigAsJson());
        }

        JsonObject data = new JsonObject();
        data.addProperty("enabled", this.enabled);
        data.addProperty("keycode", this.keycode);
        data.add("settings", settingsJson);
        return data;
    }

    public void applyConfigFromJson(JsonObject data) {
        try {
            this.keycode = data.get("keycode").getAsInt();
            this.setToggled(data.get("enabled").getAsBoolean());

            JsonObject settingsData = data.get("settings").getAsJsonObject();
            for (Value setting : this.settings) {
                if (settingsData.has(setting.getName())) {
                    setting.applyConfigFromJson(settingsData.get(setting.getName()).getAsJsonObject());
                }
            }
        } catch (NullPointerException ignored) {}
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
        if (this.enabled) {
            this.disable();
        } else {
            this.enable();
        }
    }

    public void enable() {
        this.enabled = true;
        this.onEnable();
        EventBus.INSTANCE.register(this);
    }

    public void disable() {
        this.enabled = false;
        this.onDisable();
        EventBus.INSTANCE.unregister(this);
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

    public ArrayList<Value> getSettings() {
        return this.settings;
    }

    public void addSetting(Value... settings) {
        this.settings.addAll(Arrays.asList(settings));
    }

    public Value getSettingByName(String name) {
        for (Value setting : this.settings) {
            if (setting.getName().equalsIgnoreCase(name)) {
                return setting;
            }
        }
        return null;
    }

    public void resetToDefaults() {
        this.keycode = moduleInfo.key();
        this.setToggled(moduleInfo.enabled());
        for (Value setting : this.settings) {
            setting.resetToDefaults();
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

    public boolean shouldDisplay(HUD hud) {
        Map<Category, Boolean> visibility = new HashMap<>();
        visibility.put(Category.Client, hud.hideClient.getValue());
        visibility.put(Category.Combat, hud.hideCombat.getValue());
        visibility.put(Category.Movement, hud.hideMovement.getValue());
        visibility.put(Category.Other, hud.hideOther.getValue());
        visibility.put(Category.Player, hud.hidePlayer.getValue());
        visibility.put(Category.Render, hud.hideRender.getValue());

        return !visibility.getOrDefault(this.moduleCategory, false);
    }

    public void onEnable() {}

    public void onDisable() {}

    public void update() {}

    public void guiUpdate() {}

    public void guiButtonToggled(BooleanValue setting) {}
}
