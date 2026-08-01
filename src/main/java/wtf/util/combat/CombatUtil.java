package wtf.util.combat;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityArmorStand;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.player.EntityPlayer;
import wtf.util.IMinecraft;

public class CombatUtil implements IMinecraft {

    public static boolean canTarget(final Entity entity, final boolean ignoreTeam) {
        if (entity != null && entity != mc.thePlayer) {
            EntityLivingBase entityLivingBase = null;
            if (entity instanceof EntityLivingBase) {
                entityLivingBase = (EntityLivingBase) entity;
            }
            final boolean isTeam = isTeam((EntityPlayer) mc.thePlayer, entity);
            final boolean isVisible = !entity.isInvisible();
            return !(entity instanceof EntityArmorStand)
                    && isVisible
                    && ((entity instanceof EntityPlayer && !isTeam && !ignoreTeam)
                        || entity instanceof EntityAnimal
                        || entity instanceof EntityMob
                        || (entity instanceof EntityLivingBase && entityLivingBase.isEntityAlive()));
        }
        return false;
    }
    
    public static boolean isTeam(final EntityPlayer player, final Entity entity) {
        if (entity instanceof EntityPlayer && ((EntityPlayer) entity).getTeam() != null && player.getTeam() != null) {
            final char e3 = entity.getDisplayName().getFormattedText().charAt(3);
            final char p3 = player.getDisplayName().getFormattedText().charAt(3);
            final char e4 = entity.getDisplayName().getFormattedText().charAt(2);
            final char p4 = player.getDisplayName().getFormattedText().charAt(2);
            if (e3 == p3 && e4 == p4) return true;

            final char e5 = entity.getDisplayName().getFormattedText().charAt(1);
            final char p5 = player.getDisplayName().getFormattedText().charAt(1);
            final char e6 = entity.getDisplayName().getFormattedText().charAt(0);
            final char p6 = player.getDisplayName().getFormattedText().charAt(0);
            return e5 == p5 && e6 == p6;
        }
        return true;
    }
}
