package keystrokesmod.client.module.value.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.function.Supplier;

import com.google.gson.JsonObject;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.raven.components.ModuleComponent;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.value.Value;

public class NumberValue extends Value {
    private double value;
    private final double max;
    private final double min;
    private final double interval;
    private final double defaultVal;

    public NumberValue(String name, Mod module, final double defaultValue, final double min, final double max, final double intervals, Supplier<Boolean> visible) {
    	super(name, module, visible);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.interval = intervals;
        this.defaultVal = defaultValue;
    }

    public NumberValue(String name, Mod module, final double defaultValue, final double min, final double max, final double intervals) {
        this(name, module, defaultValue, min, max, intervals, () -> true);
    }
    
    @Override
    public void resetToDefaults() {
        this.value = this.defaultVal;
    }
    
    @Override
    public JsonObject getConfigAsJson() {
        final JsonObject data = new JsonObject();
        data.addProperty("type", this.getSettingType());
        data.addProperty("value", (Number)this.getInput());
        return data;
    }
    
    @Override
    public String getSettingType() {
        return "slider";
    }
    
    @Override
    public void applyConfigFromJson(final JsonObject data) {
        if (!data.get("type").getAsString().equals(this.getSettingType())) {
            return;
        }
        this.setValue(data.get("value").getAsDouble());
    }
    
    @Override
    public Component createComponent(final ModuleComponent moduleComponent) {
        return null;
    }
    
    public double getInput() {
        return r(this.value, 2);
    }
    
    public double getMin() {
        return this.min;
    }
    
    public double getMax() {
        return this.max;
    }
    
    public void setValue(double n) {
        n = check(n, this.min, this.max);
        n = Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
        this.value = n;
    }
    
    public static double check(double v, final double i, final double a) {
        v = Math.max(i, v);
        v = Math.min(a, v);
        return v;
    }
    
    public static double r(final double v, final int p) {
        if (p < 0) {
            return 0.0;
        }
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(p, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }
}
