package keystrokesmod.client.utils.network;

import java.util.ArrayList;

import keystrokesmod.client.util.IMinecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;

public class PacketUtil implements IMinecraft {
    private static final ArrayList<Packet<?>> packets = new ArrayList<>();

    public static void sendPacketNoEvent(Packet<INetHandlerPlayServer> packet) {
        packets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
    }
}
