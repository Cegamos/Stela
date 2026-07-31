package keystrokesmod.client.module.value.impl;

import java.util.function.Supplier;

import com.google.gson.JsonObject;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.raven.components.ModuleComponent;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.Value;

public class DescriptionValue extends Value {
	private String desc;
	private String defaultDesc;

	public DescriptionValue(final String desc, Mod module, Supplier<Boolean> visible) {
		super(desc, module, visible);
		this.desc = desc;
		this.defaultDesc = desc;
	}
	
    public DescriptionValue(String name, Mod module) {
        this(name, module, () -> true);
    }

	public String getDesc() {
		return this.desc;
	}

	public void setDesc(final String t) {
		this.desc = t;
	}

	@Override
	public void resetToDefaults() {
		this.desc = defaultDesc;
	}

	@Override
	public JsonObject getConfigAsJson() {
		final JsonObject data = new JsonObject();
		data.addProperty("type", this.getSettingType());
		data.addProperty("value", this.getDesc());
		return data;
	}

	@Override
	public String getSettingType() {
		return "desc";
	}

	@Override
	public void applyConfigFromJson(final JsonObject data) {
		if (!data.get("type").getAsString().equals(this.getSettingType())) {
			return;
		}
		this.setDesc(data.get("value").getAsString());
	}

	@Override
	public Component createComponent(final ModuleComponent moduleComponent) {
		return null;
	}
}
