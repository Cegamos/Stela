package keystrokesmod.client.module.setting;

import java.util.Optional;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.raven.components.ModuleComponent;
import keystrokesmod.client.module.Mod;

public abstract class Setting {
	private final String name;
	public Supplier<Boolean> visible;

	public Setting(String name, Mod module, Supplier<Boolean> visible) {
		this.name = name;
		this.visible = visible;
		Optional.ofNullable(module).ifPresent(m -> m.addSetting(this));
	}

	public Supplier<Boolean> getVisible() {
		return visible;
	}

	public void setVisible(Supplier<Boolean> visible) {
		this.visible = visible;
	}

	public String getName() {
		return name;
	}
	
    public Boolean canDisplay() {
        return this.visible.get();
    }

	public abstract void resetToDefaults();

	public abstract JsonObject getConfigAsJson();

	public abstract String getSettingType();

	public abstract void applyConfigFromJson(final JsonObject p0);

	public abstract Component createComponent(final ModuleComponent p0);
}
