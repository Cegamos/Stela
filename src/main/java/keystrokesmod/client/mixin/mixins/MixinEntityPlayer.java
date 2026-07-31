package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.AttackEvent;
import keystrokesmod.client.event.impl.PostPlayerTickEvent;
import keystrokesmod.client.event.impl.PrePlayerTickEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@Mixin(EntityPlayer.class)
public class MixinEntityPlayer {
    @Inject(method = "attackTargetEntityWithCurrentItem", desc = "(Lnet/minecraft/entity/Entity;)V", target = @Target("TAIL"))
    private void onAttackTargetEntityWithCurrentItem(Entity target) {
    	EventBus.INSTANCE.post(new AttackEvent(target));
    }
    
    @Inject(method = "onUpdate", desc = "()V", target = @Target("HEAD"))
    public void onPreUpdate() {
    	EventBus.INSTANCE.post(new PrePlayerTickEvent());
    }
    
    @Inject(method = "onUpdate", desc = "()V", target = @Target("TAIL"))
    public void onPostUpdate() {
    	EventBus.INSTANCE.post(new PostPlayerTickEvent());
    }
}
