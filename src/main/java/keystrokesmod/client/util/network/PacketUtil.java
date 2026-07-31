package keystrokesmod.client.util.network;

import java.util.Collections;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import keystrokesmod.client.util.IMinecraft;
import net.minecraft.network.Packet;

public class PacketUtil implements IMinecraft {
    private static final Set<Packet<?>> skipPackets = Collections.newSetFromMap(new ConcurrentHashMap<>());

    public static void sendPacketNoEvent(Packet<?> packet) {
        skipPackets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static boolean checkAndRemove(Packet<?> packet) {
        return skipPackets.remove(packet);
    }
}