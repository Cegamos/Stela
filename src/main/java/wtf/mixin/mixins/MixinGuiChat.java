package wtf.mixin.mixins;

import net.minecraft.client.gui.GuiChat;
import wtf.event.EventBus;
import wtf.event.impl.DragEvent;
import wtf.stela.annotations.Inject;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.Target;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Inject(method = "drawScreen", desc = "(IIF)V", target = @Target("TAIL"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks) {
        EventBus.INSTANCE.post(new DragEvent(mouseX, mouseY, partialTicks));
    }
}