package wtf.module.value.impl;

import java.util.function.BooleanSupplier;

import wtf.module.modules.Mod;
import wtf.module.value.Value;

public class BooleanValue extends Value {
    private boolean isEnabled;
    
    private final boolean defaultValue;
    public final boolean isMethodButton;
    private final Runnable method;

    public BooleanValue(String name, Mod module, boolean value, BooleanSupplier visible) {
        super(name, module, visible);
        this.isEnabled = value;
        this.defaultValue = value;
        this.isMethodButton = false;
        this.method = null;
    }

    public BooleanValue(String name, Mod module, boolean value) {
        this(name, module, value, null);
    }

    public BooleanValue(String name, Runnable method) {
        this(name, null, method, null);
    }

    public BooleanValue(String name, Mod module, Runnable method, BooleanSupplier visibleCheck) {
        super(name, module, visibleCheck);
        this.isEnabled = false;
        this.defaultValue = false;
        this.isMethodButton = true;
        this.method = method;
    }

    @Override
    public void resetToDefaults() {
        this.isEnabled = this.defaultValue;
    }

    @Override
    public String getSettingType() {
        return "tick";
    }

    public boolean getValue() {
        return this.isEnabled;
    }

    public void toggle() {
        this.isEnabled = !this.isEnabled;
        
        if (this.isMethodButton && this.method != null) {
            this.method.run();
        }
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