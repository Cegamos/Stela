package keystrokesmod.client.util.chat;

import keystrokesmod.client.clickgui.window.TerminalWindow;
import keystrokesmod.client.util.IMinecraft;

public class ChatUtil implements IMinecraft {
    private static boolean checkingPing = false;
    protected static long pingStartTime = 0L;

    public static void checkPing() {
        TerminalWindow.print("Checking...");
        if (checkingPing) {
        	TerminalWindow.print("Please wait.");
        } else {
            if (mc.thePlayer != null) {
                mc.thePlayer.sendChatMessage("/...");
                checkingPing = true;
                pingStartTime = System.currentTimeMillis();
            }
        }
    }

    public static void reset() {
        checkingPing = false;
        pingStartTime = 0L;
    }
}
