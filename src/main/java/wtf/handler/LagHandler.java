package wtf.handler;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PacketReceiveEvent;
import wtf.util.IMinecraft;

public class LagHandler implements IMinecraft {
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
    public final Listener<PacketReceiveEvent> onPacketReceive = e -> lastPacketTime = System.currentTimeMillis();
    
}
