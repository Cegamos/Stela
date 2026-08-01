package wtf.mixin.mixins;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.network.NetHandlerPlayClient;
import net.minecraft.network.play.client.C01PacketChatMessage;
import wtf.event.EventBus;
import wtf.event.impl.ChatEvent;
import wtf.event.impl.LivingUpdateEvent;
import wtf.event.impl.PostMotionEvent;
import wtf.event.impl.PreMotionEvent;
import wtf.event.impl.PreUpdateEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Overwrite;
import wtf.stela.annotations.Shadow;
import wtf.stela.annotations.Target;

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
    
    @Inject(method = "onLivingUpdate", desc = "()V", target = @Target("HEAD"))
    private void onLivingUpdate() {
    	EventBus.INSTANCE.post(new LivingUpdateEvent());
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
