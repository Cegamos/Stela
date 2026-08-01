package wtf.module.value.impl;

import java.util.function.BooleanSupplier;

import wtf.module.modules.Mod;
import wtf.module.value.Value;

public class RangeValue extends Value {
    private double valMax;
    private double valMin;
    private final double max;
    private final double min;
    private final double interval;
    private final double defaultValMin;
    private final double defaultValMax;

    public RangeValue(String name, Mod module, final double defaultValueMin, final double defaultValueMax, final double min, final double max, final double intervals, BooleanSupplier visible) {
        super(name, module, visible);
        this.valMin = defaultValueMin;
        this.valMax = defaultValueMax;
        this.min = min;
        this.max = max;
        this.interval = intervals;
        this.defaultValMin = this.valMin;
        this.defaultValMax = this.valMax;
    }

    public RangeValue(String name, Mod module, final double defaultValueMin, final double defaultValueMax, final double min, final double max, final double intervals) {
        this(name, module, defaultValueMin, defaultValueMax, min, max, intervals, null);
    }

    @Override
    public void resetToDefaults() {
        this.setValueMin(this.defaultValMin);
        this.setValueMax(this.defaultValMax);
    }

    @Override
    public String getSettingType() {
        return "doubleslider";
    }

    public double getInputMin() {
        return rounded(this.valMin, 2);
    }

    public double getInputMax() {
        return rounded(this.valMax, 2);
    }

    public double getMin() {
        return this.min;
    }

    public double getMax() {
        return this.max;
    }

    public void setValueMin(double n) {
        n = check(n, this.min, this.max);
        n = Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
        this.valMin = n;
    }

    public void setValueMax(double n) {
        n = check(n, this.min, this.max);
        n = Math.round(n * (1.0 / this.interval)) / (1.0 / this.interval);
        this.valMax = n;
    }

    public static double check(double v, final double i, final double a) {
        return Math.min(a, Math.max(i, v));
    }

    public static double rounded(final double v, final int p) {
        if (p < 0) return 0.0;
        if (p == 0) return Math.round(v);
        
        double factor = Math.pow(10.0, p);
        return Math.round(v * factor) / factor;
    }
}