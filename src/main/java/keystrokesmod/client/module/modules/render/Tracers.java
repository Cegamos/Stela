package keystrokesmod.client.module.modules.render;

import java.awt.Color;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Tracers", category = Category.Render)
public class Tracers extends Mod {

    private final BooleanValue showInvis = new BooleanValue("Show invis", this, true);
    private final NumberValue lineWidth = new NumberValue("Line Width", this, 1.0, 1.0, 5.0, 1.0);

    private final NumberValue red = new NumberValue("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue green = new NumberValue("Green", this, 255.0, 0.0, 255.0, 1.0);
    private final NumberValue blue = new NumberValue("Blue", this, 0.0, 0.0, 255.0, 1.0);

    private final BooleanValue rainbow = new BooleanValue("Rainbow", this, false);

    private boolean previousViewBobbing;
    private int rgbColor;

    @Override
    public void onEnable() {
        previousViewBobbing = mc.gameSettings.viewBobbing;
        if (previousViewBobbing) {
            mc.gameSettings.viewBobbing = false;
        }
    }

    @Override
    public void onDisable() {
        mc.gameSettings.viewBobbing = previousViewBobbing;
    }

    @Override
    public void update() {
        if (mc.gameSettings.viewBobbing) {
            mc.gameSettings.viewBobbing = false;
        }
    }

    @Override
    public void guiUpdate() {
        rgbColor = new Color(
            (int) red.getValue(),
            (int) green.getValue(),
            (int) blue.getValue()
        ).getRGB();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Utils.Player.isPlayerInGame()) return;

        int color = rainbow.getValue() ? Utils.Client.rainbowDraw(2L, 0L) : rgbColor;
        float width = (float) lineWidth.getValue();

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.deathTime != 0) continue;
            if (!showInvis.getValue() && player.isInvisible()) continue;

            Utils.HUD.dtl(player, color, width);
        }
    }
}