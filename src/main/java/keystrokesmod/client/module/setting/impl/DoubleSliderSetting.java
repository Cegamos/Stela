package keystrokesmod.client.module.setting.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.raven.components.ModuleComponent;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.setting.Setting;

public class DoubleSliderSetting extends Setting {
    private double valMax;
    private double valMin;
    private final double max;
    private final double min;
    private final double interval;
    private final double defaultValMin;
    private final double defaultValMax;
    
    public DoubleSliderSetting(String name, Mod module, final double defaultValueMin, final double defaultValueMax, final double min, final double max, final double intervals, Supplier<Boolean> visible) {
    	super(name, module, visible);
        this.valMin = defaultValueMin;
        this.valMax = defaultValueMax;
        this.min = min;
        this.max = max;
        this.interval = intervals;
        this.defaultValMin = this.valMin;
        this.defaultValMax = this.valMax;
    }

    public DoubleSliderSetting(String name, Mod module, final double defaultValueMin, final double defaultValueMax, final double min, final double max, final double intervals) {
        this(name, module, defaultValueMin, defaultValueMax, min, max, intervals, () -> true);
    }

    @Override
    public void resetToDefaults() {
        this.setValueMin(this.defaultValMin);
        this.setValueMax(this.defaultValMax);
    }
    
    @Override
    public JsonObject getConfigAsJson() {
        final JsonObject data = new JsonObject();
        data.addProperty("type", this.getSettingType());
        data.addProperty("valueMin", this.getInputMin());
        data.addProperty("valueMax", this.getInputMax());
        return data;
    }
    
    @Override
    public String getSettingType() {
        return "doubleslider";
    }
    
    @Override
    public void applyConfigFromJson(final JsonObject data) {
        if (!data.get("type").getAsString().equals(this.getSettingType())) {
            return;
        }
        this.setValueMax(data.get("valueMax").getAsDouble());
        this.setValueMin(data.get("valueMin").getAsDouble());
    }
    
    @Override
    public Component createComponent(final ModuleComponent moduleComponent) {
        return null;
    }
    
    public double getInputMin() {
        return round(this.valMin, 2);
    }
    
    public double getInputMax() {
        return round(this.valMax, 2);
    }
    
    public double getMin() {
        return this.min;
    }
    
    public double getMax() {
        return this.max;
    }
    
    public void setValueMin(double n) {
        n = correct(n, this.min, this.valMax);
        n = Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
        this.valMin = n;
    }
    
    public void setValueMax(double n) {
        n = correct(n, this.valMin, this.max);
        n = Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
        this.valMax = n;
    }
    
    public static double correct(double val, final double min, final double max) {
        val = Math.max(min, val);
        val = Math.min(max, val);
        return val;
    }
    
    public static double round(final double val, final int p) {
        if (p < 0) {
            return 0.0;
        }
        BigDecimal bd = new BigDecimal(val);
        bd = bd.setScale(p, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
