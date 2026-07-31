package keystrokesmod.client.util.chat;

import keystrokesmod.client.clickgui.raven.Terminal;
import keystrokesmod.client.util.IMinecraft;
import net.minecraft.client.Minecraft;

public class ChatUtil implements IMinecraft {
    private static boolean checkingPing = false;
    private static long pingStartTime = 0L;

    public static void checkPing() {
        Terminal.print("Checking...");
        if (checkingPing) {
            Terminal.print("Please wait.");
        } else {
            Minecraft mc = Minecraft.getMinecraft();
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
