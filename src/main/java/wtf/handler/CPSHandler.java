package wtf.handler;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C0APacketAnimation;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.GameEvent;
import wtf.event.impl.PacketSendEvent;
import wtf.util.IMinecraft;

public class CPSHandler implements IMinecraft {
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
