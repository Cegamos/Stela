package keystrokesmod.client.clickgui.raven.components;

import java.awt.Color;
import java.util.ArrayList;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.clickgui.raven.Component;
import keystrokesmod.client.clickgui.theme.Theme;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.setting.Setting;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;

public class ModuleComponent extends Component {
    public Mod mod;
    public CategoryComponent category;
    public int o;
    public boolean open;

    private final ArrayList<Component> settings = new ArrayList<>();

    public ModuleComponent(final Mod mod, final CategoryComponent parent, final int offset) {
        this.mod = mod;
        this.category = parent;
        this.o = offset;
        this.open = false;

        int y = offset + 12;

        for (final Setting setting : mod.getSettings()) {
            Component component = createComponentForSetting(setting, y);
            if (component != null) {
                settings.add(component);
                y += (component instanceof SliderComponent || component instanceof RangeSliderComponent) ? 16 : 12;
            }
        }

        settings.add(new BindComponent(this, y));
    }

    private Component createComponentForSetting(Setting setting, int y) {
        if (setting instanceof SliderSetting)
            return new SliderComponent((SliderSetting) setting, this, y);
        if (setting instanceof TickSetting)
            return new TickComponent(mod, (TickSetting) setting, this, y);
        if (setting instanceof DescriptionSetting)
            return new DescriptionComponent((DescriptionSetting) setting, this, y);
        if (setting instanceof DoubleSliderSetting)
            return new RangeSliderComponent((DoubleSliderSetting) setting, this, y);
        if (setting instanceof ComboSetting)
            return new ModeComponent((ComboSetting) setting, this, y);
        return null;
    }

    @Override
    public void draw() {
        int x = category.getX();
        int width = category.getWidth();
        int baseY = category.getY() + o;

        GL11.glPushMatrix();
        int color;
        if (this.mod.isEnabled()) {
        	color = Theme.getMainColor().getRGB();
        } else if (this.mod.canBeEnabled()) {
        	color = Color.lightGray.getRGB();
        } else {
        	color = new Color(102, 102, 102).getRGB();
        }
        
        mc.fontRendererObj.drawStringWithShadow(mod.getName(), x + width / 2 - mc.fontRendererObj.getStringWidth(mod.getName()) / 2, baseY + 4, color);
        GL11.glPopMatrix();

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
        if (ii(x, y)) {
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

    public boolean ii(int x, int y) {
        int startY = category.getY() + o;
        return x > category.getX() && x < category.getX() + category.getWidth() && y > startY && y < startY + 16;
    }
    
    @Override
    public int height() {
        if (!open) return 16;

        return 16 + settings.stream()
            .filter(Component::isVisible)
            .mapToInt(c -> (c instanceof SliderComponent || c instanceof RangeSliderComponent) ? 16 : 12)
            .sum();
    }

    @Override
    public void setComponentStartAt(int n) {
        this.o = n;
        reflowSettings();
    }
    
    public void reflowSettings() {
        int y = o + 16;
        for (Component c : settings) {
            if (!c.isVisible()) continue;
            c.setComponentStartAt(y);
            y += (c instanceof SliderComponent || c instanceof RangeSliderComponent) ? 16 : 12;
        }
    }
}
