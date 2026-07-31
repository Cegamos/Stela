package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.DrawEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Local;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.gui.GuiIngame;

@Mixin(GuiIngame.class)
public class MixinGuiIngame {

    @Inject(method = "renderGameOverlay", desc = "(F)V", target = @Target(value = "INVOKESTATIC", target = "net/minecraft/client/renderer/GlStateManager.enableBlend()V", shift = Target.Shift.BEFORE))
    public void renderGameOverlay(@Local(source = "partialTicks", index = 1) float partialTicks) {
        EventBus.INSTANCE.post(new DrawEvent(partialTicks));
    }
}
