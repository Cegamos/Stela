package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.PostTickEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.Minecraft;

@Mixin(Minecraft.class)
public class MixinMinecraft {

    @Inject(method = "runTick", desc = "()V", target = @Target("HEAD"))
    private void onRunTickPre() {
        EventBus.INSTANCE.post(new PreTickEvent());
    }

    @Inject(method = "runTick", desc = "()V", target = @Target("TAIL"))
    private void onRunTickPost() {
    	EventBus.INSTANCE.post(new PostTickEvent());
    }

    @Inject(method = "startGame", desc = "()V", target = @Target("HEAD"))
    private void injectStartGame() {
        Raven.init();
    }
}
