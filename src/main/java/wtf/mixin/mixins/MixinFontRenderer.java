package wtf.mixin.mixins;

import net.minecraft.client.gui.FontRenderer;
import wtf.event.EventBus;
import wtf.event.impl.RenderTextEvent;
import wtf.stela.annotations.Mixin;
import wtf.stela.annotations.ModifyArg;
import wtf.stela.annotations.Target;

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