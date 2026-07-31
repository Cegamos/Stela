package keystrokesmod.client.setting;

import java.util.function.Supplier;

public abstract class Setting<T> {
    private final String name;
    private T value;
    private Supplier<Boolean> visibility;

    public Setting(String name, T defaultValue) {
        this.name = name;
        this.value = defaultValue;
        this.visibility = () -> true;
    }

    public Setting(String name, T defaultValue, Supplier<Boolean> visibility) {
        this.name = name;
        this.value = defaultValue;
        this.visibility = visibility;
    }

    public String getName() {
        return name;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }

    public boolean isVisible() {
        return visibility == null || visibility.get();
    }

    public void setVisibility(Supplier<Boolean> visibility) {
        this.visibility = visibility;
    }
}
