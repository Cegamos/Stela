package wtf.module.value.impl;

import java.util.function.BooleanSupplier;

import wtf.module.modules.Mod;
import wtf.module.value.Value;

public class DescriptionValue extends Value {
    private String desc;
    private final String defaultDesc;

    public DescriptionValue(final String desc, Mod module, BooleanSupplier visible) {
        super(desc, module, visible);
        this.desc = desc;
        this.defaultDesc = desc;
    }

    public DescriptionValue(String name, Mod module) {
        this(name, module, null);
    }

    public String getDesc() {
        return this.desc;
    }

    public void setDesc(final String t) {
        this.desc = t;
    }

    @Override
    public void resetToDefaults() {
        this.desc = this.defaultDesc;
    }

    @Override
    public String getSettingType() {
        return "desc";
    }
}