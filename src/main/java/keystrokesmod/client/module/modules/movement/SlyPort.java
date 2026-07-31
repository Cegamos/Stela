package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.world.AntiBot;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.Vec3;

@ModuleInfo(name = "SlyPort", category = Category.Movement)
public class SlyPort extends Mod {
    private final DescriptionValue f = new DescriptionValue("Teleport behind enemies.", this);
    private final NumberValue r = new NumberValue("Range", this, 6.0, 2.0, 15.0, 1.0);
    private final BooleanValue b = new BooleanValue("Play sound", this, true);
    private final BooleanValue d = new BooleanValue("Players only", this, true);
    private final BooleanValue e = new BooleanValue("Aim", this, true);
    private final boolean s = false;
    
    @Override
    public void onEnable() {
        final Entity en = this.ge();
        if (en != null) {
            this.tp(en);
        }
        this.disable();
    }
    
    private void tp(final Entity en) {
        if (b.isToggled()) {
            mc.thePlayer.playSound("mob.endermen.portal", 1.0f, 1.0f);
        }
        final Vec3 vec = en.getLookVec();
        final double x = en.posX - vec.xCoord * 2.5;
        final double z = en.posZ - vec.zCoord * 2.5;
        mc.thePlayer.setPosition(x, mc.thePlayer.posY, z);
        if (e.isToggled()) {
            Utils.Player.aim(en, 0.0f, false);
        }
    }
    
    private Entity ge() {
        Entity en = null;
        final double r = Math.pow(this.r.getInput(), 2.0);
    	AntiBot bot = (AntiBot) Raven.moduleManager.getModuleByClazz(AntiBot.class);

        double dist = r + 1.0;
        for (final Entity ent : mc.theWorld.loadedEntityList) {
            if (ent != mc.thePlayer && ent instanceof EntityLivingBase && ((EntityLivingBase)ent).deathTime == 0 && (!d.isToggled() || ent instanceof EntityPlayer)) {
                if (bot.bot(ent)) {
                    continue;
                }
                final double d = mc.thePlayer.getDistanceSqToEntity(ent);
                if (d > r || dist < d) {
                    continue;
                }
                dist = d;
                en = ent;
            }
        }
        return en;
    }
}
