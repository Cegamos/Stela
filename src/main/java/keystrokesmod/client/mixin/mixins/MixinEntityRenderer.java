package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.RenderWorldLastEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.renderer.EntityRenderer;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "renderWorldPass", desc = "(IFJ)V", target = @Target("TAIL"))
    private void onRenderWorldPass(int pass, float partialTicks, long finishTimeNano) {
        EventBus.INSTANCE.post(new RenderWorldLastEvent(partialTicks));
    }
}
