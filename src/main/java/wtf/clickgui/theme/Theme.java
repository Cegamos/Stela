package wtf.clickgui.theme;

import java.awt.Color;
import java.util.HashMap;
import java.util.Map;

import wtf.clickgui.window.ManagementWindow;

public class Theme {

	private static final Color backColor = new Color(0, 0, 0, 100);
	private static final Map<String, Color> themeColors = new HashMap<>(16, 1.0f);

	static {
		themeColors.put("PastelPink", new Color(237, 138, 209));
		themeColors.put("Cherry", new Color(255, 200, 200));
		themeColors.put("Mai", new Color(57, 46, 126));
		themeColors.put("Sassan", new Color(255, 105, 105));
		themeColors.put("Gold", new Color(255, 215, 0));
		themeColors.put("Steel", new Color(52, 152, 219));
		themeColors.put("Emerald", new Color(46, 204, 113));
		themeColors.put("Orange", new Color(255, 165, 0));
		themeColors.put("Amethyst", new Color(155, 89, 182));
		themeColors.put("Lily", new Color(76, 56, 108));
	}

	public static Color getBackColor() {
		return backColor;
	}
	
	public static Color getMainColor() {
        return themeColors.getOrDefault(ManagementWindow.currentTheme, themeColors.get("PastelPink"));
    }
}