package keystrokesmod.client.module.value.impl;

import java.util.function.Supplier;

import com.google.gson.JsonObject;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.raven.components.ModuleComponent;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.value.Value;

public class BooleanValue extends Value {
	private boolean isEnabled;
	private boolean defaultValue;
    public boolean isMethodButton;
    private Runnable method;
	
    public BooleanValue(String name, Mod module, boolean value, Supplier<Boolean> visible) {
        super(name, module, visible);
		this.isEnabled = value;
		this.defaultValue = value;
    }

    public BooleanValue(String name, Mod module, boolean value) {
        this(name, module, value, () -> true);
    }
    
    public BooleanValue(String name, Runnable method) {
        this(name, null, method, () -> true);
    }

    public BooleanValue(String name, Mod module, Runnable method, Supplier<Boolean> visibleCheck) {
		super(name, null, visibleCheck);
        this.isEnabled = false;
        this.isMethodButton = true;
        this.method = method;
    }

	@Override
	public void resetToDefaults() {
		this.isEnabled = this.defaultValue;
	}

	@Override
	public JsonObject getConfigAsJson() {
		final JsonObject data = new JsonObject();
		data.addProperty("type", this.getSettingType());
		data.addProperty("value", Boolean.valueOf(this.isToggled()));
		return data;
	}

	@Override
	public String getSettingType() {
		return "tick";
	}

	@Override
	public void applyConfigFromJson(final JsonObject data) {
		if (!data.get("type").getAsString().equals(this.getSettingType())) {
			return;
		}
		this.setEnabled(data.get("value").getAsBoolean());
	}

	@Override
	public Component createComponent(final ModuleComponent moduleComponent) {
		return null;
	}

	public boolean isToggled() {
		return this.isEnabled;
	}

	public void toggle() {
		this.isEnabled = !this.isEnabled;
	}

	public void enable() {
		this.isEnabled = true;
	}

	public void disable() {
		this.isEnabled = false;
	}

	public void setEnabled(final boolean b) {
		this.isEnabled = b;
	}
}
