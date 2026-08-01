package wtf.mixin.mixins;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import wtf.event.EventBus;
import wtf.event.impl.AttackEvent;
import wtf.event.impl.PostPlayerTickEvent;
import wtf.event.impl.PrePlayerTickEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Target;

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
