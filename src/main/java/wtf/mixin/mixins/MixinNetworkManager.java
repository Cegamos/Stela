package wtf.mixin.mixins;

import io.netty.channel.ChannelHandlerContext;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import wtf.event.EventBus;
import wtf.event.impl.PacketReceiveEvent;
import wtf.event.impl.PacketSendEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Target;
import wtf.util.network.PacketUtil;

@Mixin(NetworkManager.class)
public class MixinNetworkManager {

    @Inject(method = "sendPacket", desc = "(Lnet/minecraft/network/Packet;)V", target = @Target("HEAD"))
    private void onSendPacket(Packet<?> packet) {
    	if (!PacketUtil.checkAndRemove(packet)) {
            PacketSendEvent event = new PacketSendEvent(packet);
            EventBus.INSTANCE.post(event);
            if (event.isCancelled()) return; 
        }
    }

    @Inject(method = "channelRead0", desc = "(Lio/netty/channel/ChannelHandlerContext;Lnet/minecraft/network/Packet;)V", target = @Target("HEAD"))
    private void onReceivePacket(ChannelHandlerContext ctx, Packet<?> packet) {
    	PacketReceiveEvent event = new PacketReceiveEvent(packet);
    	EventBus.INSTANCE.post(new PacketReceiveEvent(packet));
    	if (event.isCancelled()) return;
    }
}
