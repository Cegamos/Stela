package keystrokesmod.client.module.modules.render;

import java.awt.Color;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.world.AntiBot;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "PlayerESP", category = Category.Render)
public class PlayerESP extends Mod {
    private final SliderSetting red = new SliderSetting("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final SliderSetting green = new SliderSetting("Green", this, 255.0, 0.0, 255.0, 1.0);
    private final SliderSetting blue = new SliderSetting("Blue", this, 0.0, 0.0, 255.0, 1.0);
    private final TickSetting rainbow = new TickSetting("Rainbow", this, false);

    private final DescriptionSetting espTypes = new DescriptionSetting("ESP Types", this);

    private final TickSetting esp2D = new TickSetting("2D", this, false);
    private final TickSetting arrow = new TickSetting("Arrow", this, false);
    private final TickSetting box = new TickSetting("Box", this, false);
    private final TickSetting health = new TickSetting("Health", this, true);
    private final TickSetting ring = new TickSetting("Ring", this, false);
    private final TickSetting shaded = new TickSetting("Shaded", this, false);

    private final SliderSetting expand = new SliderSetting("Expand", this, 0.0, -0.3, 2.0, 0.1);
    private final SliderSetting xShift = new SliderSetting("X-Shift", this, 0.0, -35.0, 10.0, 1.0);

    private final TickSetting showInvis = new TickSetting("Show invis", this, true);
    private final TickSetting redOnDamage = new TickSetting("Red on damage", this, true);

    private int rgb_c = 0;

    @Override
    public void onDisable() {
        Utils.HUD.ring_c = false;
    }

    @Override
    public void guiUpdate() {
        this.rgb_c = new Color(
            (int) red.getInput(),
            (int) green.getInput(),
            (int) blue.getInput()
        ).getRGB();
    }

    @SubscribeEvent
    public void r1(final RenderWorldLastEvent e) {
        if (!Utils.Player.isPlayerInGame()) return;

        final int rgb = rainbow.isToggled() ? 0 : this.rgb_c;
    	AntiBot bot = (AntiBot) Raven.moduleManager.getModuleByClazz(AntiBot.class);

        if (!Raven.debugger) {
            for (final EntityPlayer en : mc.theWorld.playerEntities) {
                if (en != mc.thePlayer && en.deathTime == 0 && (showInvis.isToggled() || !en.isInvisible())) {
                    if (bot.bot(en)) continue;
                    this.renderESP(en, rgb);
                }
            }
        } else {
            for (final Entity en2 : mc.theWorld.loadedEntityList) {
                if (en2 instanceof EntityLivingBase && en2 != mc.thePlayer) {
                    this.renderESP(en2, rgb);
                }
            }
        }
    }

    private void renderESP(final Entity en, final int rgb) {
        if (box.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 1, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
        if (shaded.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 2, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
        if (esp2D.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 3, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
        if (health.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 4, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
        if (arrow.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 5, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
        if (ring.isToggled()) {
            Utils.HUD.drawBoxAroundEntity(en, 6, expand.getInput(), xShift.getInput(), rgb, redOnDamage.isToggled());
        }
    }
}
