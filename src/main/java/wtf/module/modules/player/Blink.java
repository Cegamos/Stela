package wtf.module.modules.player;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.client.C03PacketPlayer;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C08PacketPlayerBlockPlacement;
import net.minecraft.network.play.client.C09PacketHeldItemChange;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PacketSendEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.util.network.PacketUtil;

@ModuleInfo(name = "Blink", category = Category.Player)
public class Blink extends Mod {
    private Queue<Packet<?>> packets = new ConcurrentLinkedQueue<>();
    
    @Override
    public void onDisable() {
        super.onDisable();
        
        while (!packets.isEmpty()) {
            PacketUtil.sendPacketNoEvent(packets.poll());
        }
    }
    
    @EventLink
    private Listener<PacketSendEvent> packetSend = event -> {
        if (mc.isSingleplayer()) return;
        
        Packet<?> packet = event.getPacket();
        
        if (packet instanceof C03PacketPlayer || 
            packet instanceof C08PacketPlayerBlockPlacement || 
            packet instanceof C07PacketPlayerDigging || 
            packet instanceof C09PacketHeldItemChange || 
            packet instanceof C02PacketUseEntity ||
            packet instanceof C0APacketAnimation || 
            packet instanceof C0BPacketEntityAction) {
            
            packets.add(packet);
            event.cancel();
        }
    };
}