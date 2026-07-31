package keystrokesmod.client.module.value.impl;

import java.util.function.BooleanSupplier;

import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.Value;

public class NumberValue extends Value {
    private double value;
    private final double max;
    private final double min;
    private final double interval;
    private final double defaultVal;

    public NumberValue(String name, Mod module, final double defaultValue, final double min, final double max, final double intervals, BooleanSupplier visible) {
        super(name, module, visible);
        this.value = defaultValue;
        this.min = min;
        this.max = max;
        this.interval = intervals;
        this.defaultVal = defaultValue;
    }

    public NumberValue(String name, Mod module, final double defaultValue, final double min, final double max, final double intervals) {
        this(name, module, defaultValue, min, max, intervals, null);
    }

    @Override
    public void resetToDefaults() {
        this.value = this.defaultVal;
    }

    @Override
    public String getSettingType() {
        return "slider";
    }

    public double getValue() {
        return rounded(this.value, 2);
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
        return Math.min(a, Math.max(i, v)); // Más limpio y directo
    }

    public static double rounded(final double v, final int p) {
        if (p < 0) return 0.0;
        if (p == 0) return Math.round(v);
        
        double factor = Math.pow(10.0, p);
        return Math.round(v * factor) / factor;
    }
}