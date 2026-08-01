package keystrokesmod.client.module.modules.render;

import java.awt.Color;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "PlayerESP", category = Category.Render)
public class PlayerESP extends Mod {
    private final NumberValue red = new NumberValue("Red", this, 0.0, 0.0, 255.0, 1.0);
    private final NumberValue green = new NumberValue("Green", this, 255.0, 0.0, 255.0, 1.0);
    private final NumberValue blue = new NumberValue("Blue", this, 0.0, 0.0, 255.0, 1.0);
    private final BooleanValue rainbow = new BooleanValue("Rainbow", this, false);

    private final DescriptionValue espTypes = new DescriptionValue("ESP Types", this);

    private final BooleanValue esp2D = new BooleanValue("2D", this, false);
    private final BooleanValue arrow = new BooleanValue("Arrow", this, false);
    private final BooleanValue box = new BooleanValue("Box", this, false);
    private final BooleanValue health = new BooleanValue("Health", this, true);
    private final BooleanValue ring = new BooleanValue("Ring", this, false);
    private final BooleanValue shaded = new BooleanValue("Shaded", this, false);

    private final NumberValue expand = new NumberValue("Expand", this, 0.0, -0.3, 2.0, 0.1);
    private final NumberValue xShift = new NumberValue("X-Shift", this, 0.0, -35.0, 10.0, 1.0);

    private final BooleanValue showInvis = new BooleanValue("Show invis", this, true);
    private final BooleanValue redOnDamage = new BooleanValue("Red on damage", this, true);

    private int rgb_c = 0;

    @Override
    public void onDisable() {
        Utils.HUD.ring_c = false;
    }

    @Override
    public void guiUpdate() {
        this.rgb_c = new Color(
            (int) red.getValue(),
            (int) green.getValue(),
            (int) blue.getValue()
        ).getRGB();
    }

    @SubscribeEvent
    public void r1(final RenderWorldLastEvent e) {
        if (!Utils.Player.isPlayerInGame()) return;

        final int rgb = rainbow.getValue() ? 0 : this.rgb_c;
        for (final EntityPlayer en : mc.theWorld.playerEntities) {
            if (en != mc.thePlayer && en.deathTime == 0 && (showInvis.getValue() || !en.isInvisible())) {
                this.renderESP(en, rgb);
            }
        }
    }

    private void renderESP(final Entity en, final int rgb) {
        if (box.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 1, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
        if (shaded.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 2, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
        if (esp2D.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 3, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
        if (health.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 4, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
        if (arrow.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 5, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
        if (ring.getValue()) {
            Utils.HUD.drawBoxAroundEntity(en, 6, expand.getValue(), xShift.getValue(), rgb, redOnDamage.getValue());
        }
    }
}
