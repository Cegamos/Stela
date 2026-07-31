package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.DragEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.gui.GuiChat;

@Mixin(GuiChat.class)
public class MixinGuiChat {

    @Inject(method = "drawScreen", desc = "(IIF)V", target = @Target("TAIL"))
    private void onDrawScreen(int mouseX, int mouseY, float partialTicks) {
        EventBus.INSTANCE.post(new DragEvent(mouseX, mouseY, partialTicks));
    }
}