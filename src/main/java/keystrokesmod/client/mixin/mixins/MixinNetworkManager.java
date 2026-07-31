package keystrokesmod.client.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.PacketReceiveEvent;
import keystrokesmod.client.event.impl.PacketSendEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket", desc = "(Lnet/minecraft/network/Packet;)V", target = @Target("HEAD"))
    private void onSendPacket(Packet<?> packet) {
    	PacketSendEvent event = new PacketSendEvent(packet);
        EventBus.INSTANCE.post(new PacketSendEvent(packet));
	
    	if (event.isCancelled()) return;
    }

    @Inject(method = "channelRead0", desc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", target = @Target("HEAD"))
    private void onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet) {
    	PacketReceiveEvent event = new PacketReceiveEvent(packet);
    	EventBus.INSTANCE.post(new PacketReceiveEvent(packet));
    	if (event.isCancelled()) return;    	
    }
}
