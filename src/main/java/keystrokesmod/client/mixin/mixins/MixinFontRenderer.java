package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.RenderTextEvent;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.ModifyArg;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.gui.FontRenderer;

@Mixin(FontRenderer.class)
public class MixinFontRenderer {

    @ModifyArg(method = "renderString", desc = "(Ljava/lang/String;FFIZ)I", target = @Target("HEAD"), index = 0)
    public static String renderString(String text) {
        if (text == null) {
            return null;
        }
        RenderTextEvent event = new RenderTextEvent(text);
        EventBus.INSTANCE.post(event);
        return event.getText();
    }

    @ModifyArg(method = "getStringWidth", desc = "(Ljava/lang/String;)I", target = @Target("HEAD"), index = 0)
    public static String getStringWidth(String text) {
        if (text == null) {
            return null;
        }
        RenderTextEvent event = new RenderTextEvent(text);
        EventBus.INSTANCE.post(event);
        return event.getText();
    }
}