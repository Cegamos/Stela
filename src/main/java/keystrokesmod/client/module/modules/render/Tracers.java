package keystrokesmod.client.module.modules.render;

import java.awt.Color;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.world.AntiBot;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Tracers", category = Category.Render)
public class Tracers extends Mod {

    private final TickSetting showInvis = new TickSetting("Show invis", this, true);
    private final SliderSetting lineWidth = new SliderSetting("Line Width", this, 1.0, 1.0, 5.0, 1.0);

    private final SliderSetting red = new SliderSetting("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final SliderSetting green = new SliderSetting("Green", this, 255.0, 0.0, 255.0, 1.0);
    private final SliderSetting blue = new SliderSetting("Blue", this, 0.0, 0.0, 255.0, 1.0);

    private final TickSetting rainbow = new TickSetting("Rainbow", this, false);

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
            (int) red.getInput(),
            (int) green.getInput(),
            (int) blue.getInput()
        ).getRGB();
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Utils.Player.isPlayerInGame()) return;

        AntiBot botModule = (AntiBot) Raven.moduleManager.getModuleByClazz(AntiBot.class);
        int color = rainbow.isToggled() ? Utils.Client.rainbowDraw(2L, 0L) : rgbColor;
        float width = (float) lineWidth.getInput();

        if (!Raven.debugger) {
            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (player == mc.thePlayer || player.deathTime != 0) continue;
                if (!showInvis.isToggled() && player.isInvisible()) continue;
                if (botModule.bot(player)) continue;

                Utils.HUD.dtl(player, color, width);
            }
        } else {
            for (Entity entity : mc.theWorld.loadedEntityList) {
                if (entity instanceof EntityLivingBase && entity != mc.thePlayer) {
                    Utils.HUD.dtl(entity, color, width);
                }
            }
        }
    }
}