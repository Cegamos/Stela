package keystrokesmod.client.clickgui.component.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import keystrokesmod.client.clickgui.component.ModuleButton;
import keystrokesmod.client.module.value.impl.NumberValue;

public class SliderSetting extends AbstractSlider {
    private final NumberValue value;
    private boolean dragging;
    private double currentWidth;

    public SliderSetting(final NumberValue setting, final ModuleButton module, final int offset) {
        super(module, offset);
        this.value = setting;
        this.dragging = false;
    }

    @Override
    public void draw() {
        int trackX = this.module.getCategory().getX() + 8;
        int trackY = this.module.getCategory().getY() + this.offset + 12;
        int trackWidth = this.module.getCategory().getWidth() - 16;
        int fillWidth = (int) Math.min(trackWidth, Math.max(0, this.currentWidth));

        drawSlider(trackX, trackY, trackWidth, trackX, fillWidth, this.value.getName(), String.valueOf(this.value.getValue()));
    }

    @Override
    public void update(final int mousePosX, final int mousePosY) {
        int trackWidth = this.module.getCategory().getWidth() - 16;
        int trackX = this.module.getCategory().getX() + 8;

        final double relativeMouse = Math.min(trackWidth, Math.max(0, mousePosX - trackX));
        this.currentWidth = trackWidth * (this.value.getValue() - this.value.getMin()) / (this.value.getMax() - this.value.getMin());

        if (this.dragging) {
            if (relativeMouse == 0.0) {
                this.value.setValue(this.value.getMin());
            } else {
                final double calc = round(relativeMouse / trackWidth * (this.value.getMax() - this.value.getMin()) + this.value.getMin(), 2);
                this.value.setValue(calc);
            }
        }
    }

    private static double round(final double v, final int p) {
        if (p < 0) return 0.0;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(p, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(final int x, final int y, final int b) {
        if (!this.module.isVisible()) return;
        if (isHovered(x, y) && b == 0 && this.module.open) {
            this.dragging = true;
        }
    }

    @Override
    public void mouseReleased(final int x, final int y, final int m) {
        this.dragging = false;
    }

    @Override
    public boolean isVisible() {
        return this.value.canDisplay();
    }

    public boolean isHovered(final int x, final int y) {
        int trackX = this.module.getCategory().getX() + 4;
        int trackY = this.module.getCategory().getY() + this.offset;
        return x >= trackX && x <= trackX + this.module.getCategory().getWidth() - 8 && y >= trackY + 2 && y <= trackY + 16;
    }
}
