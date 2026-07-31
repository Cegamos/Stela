package keystrokesmod.client.clickgui.component;

import keystrokesmod.client.util.IMinecraft;

public abstract class Component implements IMinecraft {
    public float x, y, width, height;
    
    public void draw() {}

    public void update(final int mouseX, final int mouseY) {}

    public void mouseDown(final int mouseX, final int mouseY, final int button) {}

    public void mouseReleased(final int mouseX, final int mouseY, final int button) {}

    public void keyTyped(final char typedChar, final int keyCode) {}

    public void setComponentStartAt(final int offset) {}

    public int height() {
        return 0;
    }
    
    public boolean isVisible() {
        return true;
    }
}
