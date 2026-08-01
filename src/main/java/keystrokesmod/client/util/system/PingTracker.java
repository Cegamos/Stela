package keystrokesmod.client.util.system;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PacketReceiveEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.util.IMinecraft;
import net.minecraft.network.play.client.C14PacketTabComplete;
import net.minecraft.network.play.server.S3APacketTabComplete;

public class PingTracker implements IMinecraft {
    private static long sendTime;
    private static int ping = 0;
    private static boolean waitingForPong;
    private static long lastUpdate;

    public static int getPing() {
        return ping;
    }

    public static int getTickPing() {
        return (int) Math.ceil((float) ping / 50F);
    }

    @EventLink
    public final Listener<PreTickEvent> onTick = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        long now = System.currentTimeMillis();
        if (now - lastUpdate > 10000L) {
            lastUpdate = now;
            if (mc.getNetHandler() != null && mc.thePlayer.getName() != null) {
                mc.getNetHandler().addToSendQueue(new C14PacketTabComplete(String.valueOf(mc.thePlayer.getName().charAt(0))));
                sendTime = now;
                waitingForPong = true;
            }
        }
    };

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = e -> {
        if (e.getPacket() instanceof S3APacketTabComplete && waitingForPong) {
            ping = (int) (System.currentTimeMillis() - sendTime);
            waitingForPong = false;
        }
    };
}
