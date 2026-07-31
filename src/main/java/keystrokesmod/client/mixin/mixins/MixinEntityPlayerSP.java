package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.ChatEvent;
import keystrokesmod.client.event.impl.PostMotionEvent;
import keystrokesmod.client.event.impl.PreMotionEvent;
import keystrokesmod.client.event.impl.PreUpdateEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Overwrite;
import keystrokesmod.client.stela.annotations.Shadow;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C01PacketChatMessage;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
	
    @Shadow
    public NetHandlerPlayClient sendQueue = null;
    
    @Inject(method = "onUpdate", desc = "()V", target = @Target("HEAD"))
    private void onUpdate() {
    	EventBus.INSTANCE.post(new PreUpdateEvent());
    }
	
    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("HEAD"))
    private void onPreMotion() {
        EventBus.INSTANCE.post(new PreMotionEvent());
    }

    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("TAIL"))
    private void onPostMotion() {
    	EventBus.INSTANCE.post(new PostMotionEvent());
    }
    
    @Overwrite(method = "sendChatMessage", desc = "(Ljava/lang/String;)V")
    public void sendChatMessage(String message) {
        ChatEvent event = new ChatEvent(message);
        EventBus.INSTANCE.post(event);
        if (!event.isCancelled()) {
            message = event.getMessage();
            this.sendQueue.addToSendQueue(new C01PacketChatMessage(message));
        }
    }
}
