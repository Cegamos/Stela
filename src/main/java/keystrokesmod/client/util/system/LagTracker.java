package keystrokesmod.client.util.system;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PacketReceiveEvent;
import keystrokesmod.client.util.IMinecraft;

public class LagTracker implements IMinecraft {
    private static long lastPacketTime = System.currentTimeMillis();
    private static long lagStartTime = System.currentTimeMillis();

    public static boolean isLagging() {
        long now = System.currentTimeMillis();
        boolean elapsed = (now - lastPacketTime) > 250L;
        return elapsed && (now - lagStartTime) > 75L;
    }

    public static long getLastPacketTime() {
        return lastPacketTime;
    }

    @EventLink
    public final Listener<PacketReceiveEvent> onPacketReceive = e -> {
        lastPacketTime = System.currentTimeMillis();
    };
}
