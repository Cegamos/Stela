package keystrokesmod.client.module.modules.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PacketSendEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;

@ModuleInfo(name = "Blink", category = Category.Player)
public class Blink extends Mod {
	private Clock clock = new Clock(0);
	private Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
	
	@Override
	public void onDisable() {
	    if (mc.thePlayer == null || mc.thePlayer.sendQueue == null) {
	        packets.clear();
	        return;
	    }

	    if (!packets.isEmpty()) {
	        for (Packet<?> packet : packets) {
	            mc.thePlayer.sendQueue.addToSendQueue(packet);
	        }
	        clock.start();
	    }

	    packets.clear();
	}
	
    @EventLink
    private Listener<PacketSendEvent> packetSend = event -> {
        if (event.getPacket() instanceof C03PacketPlayer) {
            packets.add(event.getPacket());
            event.cancel();
         } else if (event.getPacket() instanceof C08PacketPlayerBlockPlacement || event.getPacket() instanceof C07PacketPlayerDigging || event.getPacket() instanceof C09PacketHeldItemChange || event.getPacket() instanceof C02PacketUseEntity) {
            this.packets.add(event.getPacket());
            event.cancel();
         }
    };
}
