package wtf.clickgui.component.impl;

import java.math.BigDecimal;
import java.math.RoundingMode;

import wtf.clickgui.component.ModuleButton;
import wtf.module.value.impl.RangeValue;

public class RangeSliderSetting extends AbstractSlider {
    private final RangeValue rangeValue;
    private double blankWidth;
    private double barWidth;
    private DragMode dragMode;

    public RangeSliderSetting(RangeValue rangeValue, ModuleButton module, int offset) {
        super(module, offset);
        this.dragMode = DragMode.NONE;
        this.rangeValue = rangeValue;
    }

    @Override
    public void draw() {
        int trackX = this.module.getCategory().getX() + 8;
        int trackY = this.module.getCategory().getY() + this.offset + 12;
        int trackWidth = this.module.getCategory().getWidth() - 16;

        int fillX = trackX + (int) this.blankWidth;
        int fillWidth = (int) this.barWidth;

        String valueLabel = this.rangeValue.getInputMin() + " - " + this.rangeValue.getInputMax();

        drawSlider(trackX, trackY, trackWidth, fillX, fillWidth, this.rangeValue.getName(), valueLabel);
    }

    @Override
    public void update(int mouseX, int mouseY) {
        int trackX = this.module.getCategory().getX() + 8;
        int trackWidth = this.module.getCategory().getWidth() - 16;
        double min = this.rangeValue.getMin();
        double max = this.rangeValue.getMax();

        this.blankWidth = trackWidth * (this.rangeValue.getInputMin() - min) / (max - min);
        this.barWidth = trackWidth * (this.rangeValue.getInputMax() - this.rangeValue.getInputMin()) / (max - min);

        double relativeMouse = Math.min(trackWidth, Math.max(0, mouseX - trackX));

        if (this.dragMode == DragMode.LEFT) {
            double val = round(relativeMouse / trackWidth * (max - min) + min, 2);
            if (val < min) val = min;
            if (val > this.rangeValue.getInputMax()) val = this.rangeValue.getInputMax();

            this.rangeValue.setValueMin(val);
        } else if (this.dragMode == DragMode.RIGHT) {
            double val = round(relativeMouse / trackWidth * (max - min) + min, 2);
            if (val > max) val = max;
            if (val < this.rangeValue.getInputMin()) val = this.rangeValue.getInputMin();

            this.rangeValue.setValueMax(val);
        }
    }

    private static double round(double v, int p) {
        if (p < 0) return 0.0;
        BigDecimal bd = new BigDecimal(v);
        bd = bd.setScale(p, RoundingMode.HALF_UP);
        return bd.doubleValue();
    }

    @Override
    public void mouseDown(int mouseX, int mouseY, int button) {
        if (button == 0 && this.module.open && isHovered(mouseX, mouseY)) {
            int trackX = this.module.getCategory().getX() + 8;
            int middle = (int) (trackX + this.blankWidth + (this.barWidth / 2.0));
            if (mouseX < middle) {
                this.dragMode = DragMode.LEFT;
            } else {
                this.dragMode = DragMode.RIGHT;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) {
            this.dragMode = DragMode.NONE;
        }
    }

    @Override
    public boolean isVisible() {
        return this.rangeValue.canDisplay();
    }

    public boolean isHovered(int mouseX, int mouseY) {
        int trackX = this.module.getCategory().getX() + 4;
        int trackY = this.offset + this.module.getCategory().getY();
        return mouseX >= trackX && mouseX <= trackX + this.module.getCategory().getWidth() - 8 && mouseY >= trackY && mouseY <= trackY + 16;
    }

    private enum DragMode {
        LEFT, RIGHT, NONE
    }
}
