package keystrokesmod.client.util.system;

import java.util.ArrayList;
import java.util.List;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.GameEvent;
import keystrokesmod.client.event.impl.PacketSendEvent;
import keystrokesmod.client.util.IMinecraft;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;

public class CPSTracker implements IMinecraft {
    private static final List<Long> leftClicks = new ArrayList<>();
    private static final List<Long> rightClicks = new ArrayList<>();

    public static int getLeftClicks() {
        return leftClicks.size();
    }

    public static int getRightClicks() {
        return rightClicks.size();
    }

    @EventLink
    public final Listener<GameEvent> game = e -> {
        long time = System.currentTimeMillis();
        leftClicks.removeIf(aLong -> aLong + 1000L < time);
        rightClicks.removeIf(aLong -> aLong + 1000L < time);
    };

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = event -> {
        if (event.getPacket() instanceof C0APacketAnimation) {
            leftClicks.add(System.currentTimeMillis());
        }
        
        if (event.getPacket() instanceof C08PacketPlayerBlockPlacement) {
            rightClicks.add(System.currentTimeMillis());
        }
    };
}
