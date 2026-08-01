package wtf.clickgui.component;

import java.awt.Color;
import java.util.ArrayList;

import wtf.clickgui.component.impl.*;
import wtf.clickgui.theme.Theme;
import wtf.module.modules.Mod;
import wtf.module.value.Value;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.DescriptionValue;
import wtf.module.value.impl.ModeValue;
import wtf.module.value.impl.NumberValue;
import wtf.module.value.impl.RangeValue;
import wtf.util.render.RoundedUtil;
import wtf.util.render.animation.Animation;
import wtf.util.render.animation.Direction;
import wtf.util.render.animation.SmoothStepAnimation;

public class ModuleButton extends Component {
    public Mod mod;
    public CategoryPanel category;
    public int offset;
    public boolean open;

    private final ArrayList<Component> settings = new ArrayList<>();
    private final Animation enableAnimation = new SmoothStepAnimation(150, 1.0);

    public ModuleButton(final Mod mod, final CategoryPanel parent, final int offset) {
        this.mod = mod;
        this.category = parent;
        this.offset = offset;
        this.open = false;

        int y = offset + 12;

        for (final Value setting : mod.getSettings()) {
            Component component = createComponentForSetting(setting, y);
            if (component != null) {
                settings.add(component);
                y += (component instanceof SliderSetting || component instanceof RangeSliderSetting) ? 16 : 12;
            }
        }

        settings.add(new BindSetting(this, y));
    }

    private Component createComponentForSetting(Value setting, int y) {
        if (setting instanceof NumberValue)
            return new SliderSetting((NumberValue) setting, this, y);
        if (setting instanceof BooleanValue)
            return new CheckboxSetting(mod, (BooleanValue) setting, this, y);
        if (setting instanceof DescriptionValue)
            return new TextSetting((DescriptionValue) setting, this, y);
        if (setting instanceof RangeValue)
            return new RangeSliderSetting((RangeValue) setting, this, y);
        if (setting instanceof ModeValue)
            return new ModeSetting((ModeValue) setting, this, y);
        return null;
    }

    @Override
    public void draw() {
        int x = category.getX();
        int width = category.getWidth();
        int baseY = category.getY() + offset;

        enableAnimation.setDirection(this.mod.isEnabled() ? Direction.FORWARDS : Direction.BACKWARDS);
        double anim = enableAnimation.getOutput();

        if (anim > 0.0) {
            Color mainColor = Theme.getMainColor();
            int alpha = (int) (50 * anim);
            Color pillBg = new Color(mainColor.getRed(), mainColor.getGreen(), mainColor.getBlue(), alpha);
            RoundedUtil.drawRound(x + 4, baseY + 1, width - 8, 14, 3f, pillBg);
        }

        Color textColor;
        if (!this.mod.canBeEnabled()) {
            textColor = new Color(90, 92, 105);
        } else {
            Color disabledColor = new Color(200, 202, 215);
            Color mainColor = Theme.getMainColor();
            textColor = interpolateColor(disabledColor, mainColor, (float) anim);
        }

        // Draw Module Name
        mc.fontRendererObj.drawStringWithShadow(
            mod.getName(),
            x + width / 2 - mc.fontRendererObj.getStringWidth(mod.getName()) / 2,
            baseY + 4,
            textColor.getRGB()
        );

        // Settings Dropdown Indicator Dot removed as requested

        if (open) {
            category.r3nd3r();
            reflowSettings();
            for (Component c : settings) {
                if (c.isVisible()) {
                    c.draw();
                }
            }
        }
    }

    @Override
    public void update(int mouseX, int mouseY) {
        for (Component c : settings) {
            c.update(mouseX, mouseY);
        }
    }

    @Override
    public void mouseDown(int x, int y, int b) {
        if (isHovered(x, y)) {
            if (b == 0 && mod.canBeEnabled()) mod.toggle();
            else if (b == 1) {
                open = !open;
                category.r3nd3r();
            }
        }

        for (Component c : settings) {
            c.mouseDown(x, y, b);
        }
    }

    @Override
    public void mouseReleased(int x, int y, int m) {
        for (Component c : settings) {
            c.mouseReleased(x, y, m);
        }
    }

    @Override
    public void keyTyped(char t, int k) {
        for (Component c : settings) {
            c.keyTyped(t, k);
        }
    }

    public boolean isHovered(int mouseX, int mouseY) {
        int startY = category.getY() + offset;
        return mouseX > category.getX() && mouseX < category.getX() + category.getWidth() && mouseY > startY && mouseY < startY + 16;
    }

    @Override
    public int height() {
        if (!open) return 16;

        return 16 + settings.stream()
            .filter(Component::isVisible)
            .mapToInt(c -> (c instanceof SliderSetting || c instanceof RangeSliderSetting) ? 16 : 12)
            .sum();
    }

    @Override
    public void setComponentStartAt(int n) {
        this.offset = n;
        reflowSettings();
    }

    public void reflowSettings() {
        int y = offset + 16;
        for (Component c : settings) {
            if (!c.isVisible()) continue;
            c.setComponentStartAt(y);
            y += (c instanceof SliderSetting || c instanceof RangeSliderSetting) ? 16 : 12;
        }
    }

    public CategoryPanel getCategory() {
        return category;
    }

    private Color interpolateColor(Color color1, Color color2, float factor) {
        factor = Math.max(0.0f, Math.min(1.0f, factor));
        int r = (int) (color1.getRed() + factor * (color2.getRed() - color1.getRed()));
        int g = (int) (color1.getGreen() + factor * (color2.getGreen() - color1.getGreen()));
        int b = (int) (color1.getBlue() + factor * (color2.getBlue() - color1.getBlue()));
        int a = (int) (color1.getAlpha() + factor * (color2.getAlpha() - color1.getAlpha()));
        return new Color(r, g, b, a);
    }
}
