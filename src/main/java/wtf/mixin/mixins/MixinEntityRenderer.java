package wtf.mixin.mixins;

import net.minecraft.client.renderer.EntityRenderer;
import wtf.event.EventBus;
import wtf.event.impl.RenderWorldLastEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Target;

@Mixin(EntityRenderer.class)
public class MixinEntityRenderer {

    @Inject(method = "renderWorldPass", desc = "(IFJ)V", target = @Target("TAIL"))
    private void onRenderWorldPass(int pass, float partialTicks, long finishTimeNano) {
        EventBus.INSTANCE.post(new RenderWorldLastEvent(partialTicks));
    }
}
