package keystrokesmod.client.clickgui.theme;

import java.awt.Color;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.modules.client.GuiModule;

public class Theme {
    public static Color getMainColor() {
    	GuiModule gui = (GuiModule) Raven.moduleManager.getModuleByClazz(GuiModule.class);
        switch (gui.mode.getMode()) {
            case "PastelPink":
                return new Color(237, 138, 209);
            case "Cherry":
                return new Color(255, 200, 200);
            case "Mai":
                return new Color(57, 46, 126);
            case "Sassan":
                return new Color(255, 105, 105);
            case "Gold":
                return new Color(255, 215, 0);
            case "Steel":
                return new Color(52, 152, 219);
            case "Emerald":
                return new Color(46, 204, 113);
            case "Orange":
                return new Color(255, 165, 0);
            case "Amethyst":
                return new Color(155, 89, 182);
            case "Lily":
                return new Color(76, 56, 108);
            default: // UNUSED
                return new Color(255, 255, 255);
        }
    }

    public static Color getBackColor() {
        return new Color(0, 0, 0, 100);
    }
}