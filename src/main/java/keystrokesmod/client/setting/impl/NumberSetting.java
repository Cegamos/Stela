package keystrokesmod.client.setting.impl;

import keystrokesmod.client.setting.Setting;

import java.util.function.Supplier;

public class NumberSetting extends Setting<Double> {
    private final double min;
    private final double max;
    private final double step;

    public NumberSetting(String name, double defaultValue, double min, double max, double step) {
        super(name, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    public NumberSetting(String name, double defaultValue, double min, double max, double step, Supplier<Boolean> visibility) {
        super(name, defaultValue, visibility);
        this.min = min;
        this.max = max;
        this.step = step;
    }

    @Override
    public void setValue(Double value) {
        double clamped = Math.max(min, Math.min(max, value));
        double stepped = Math.round(clamped / step) * step;
        super.setValue(stepped);
    }

    public double getMin() {
        return min;
    }

    public double getMax() {
        return max;
    }

    public double getStep() {
        return step;
    }
}
