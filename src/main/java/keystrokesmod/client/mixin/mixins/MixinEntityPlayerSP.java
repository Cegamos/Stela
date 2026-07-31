package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.PostMotionEvent;
import keystrokesmod.client.event.impl.PreMotionEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.entity.EntityPlayerSP;

@Mixin(EntityPlayerSP.class)
public class MixinEntityPlayerSP {
	
    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("HEAD"))
    private void onPreMotion() {
        EventBus.INSTANCE.post(new PreMotionEvent());
    }

    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("TAIL"))
    private void onPostMotion() {
    	EventBus.INSTANCE.post(new PostMotionEvent());
    }
}
