package keystrokesmod.client.module.modules.combat;

import java.util.List;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.item.EntityItemFrame;
import net.minecraft.init.Blocks;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraftforge.client.event.MouseEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "Reach", category = Category.Combat)
public class Reach extends Mod {
    private final RangeValue reach = new RangeValue("Reach (Blocks)", this, 3.1, 3.3, 3.0, 6.0, 0.05);
    private final BooleanValue weapon_only = new BooleanValue("Weapon only", this, false);
    private final BooleanValue moving_only = new BooleanValue("Moving only", this, false);
    private final BooleanValue sprint_only = new BooleanValue("Sprint only", this, false);
    private final BooleanValue hit_through_blocks = new BooleanValue("Hit through blocks", this, false);

    @SubscribeEvent
    public void onMouse(final MouseEvent ev) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        final Mod autoClicker = Raven.moduleManager.getModuleByClazz(LeftClicker.class);
        if (autoClicker != null && autoClicker.isEnabled() && Mouse.isButtonDown(0)) {
            return;
        }
        if (ev.button >= 0 && ev.buttonstate) {
            call();
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent ev) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        final Mod autoClicker = Raven.moduleManager.getModuleByClazz(LeftClicker.class);
        if (autoClicker == null || !autoClicker.isEnabled()) {
            return;
        }
        if (autoClicker.isEnabled() && Mouse.isButtonDown(0)) {
            call();
        }
    }
    
    public boolean call() {
        if (!Utils.Player.isPlayerInGame()) {
            return false;
        }
        if (weapon_only.getValue() && !Utils.Player.isPlayerHoldingWeapon()) {
            return false;
        }
        if (moving_only.getValue() && mc.thePlayer.moveForward == 0.0 && mc.thePlayer.moveStrafing == 0.0) {
            return false;
        }
        if (sprint_only.getValue() && !mc.thePlayer.isSprinting()) {
            return false;
        }
        if (!hit_through_blocks.getValue() && mc.objectMouseOver != null) {
            final BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null && mc.theWorld.getBlockState(p).getBlock() != Blocks.air) {
                return false;
            }
        }
        final double r = Utils.Client.ranModuleVal(reach, Utils.Java.rand());
        final Object[] o = zz(r, 0.0);
        if (o == null) {
            return false;
        }
        final Entity en = (Entity)o[0];
        mc.objectMouseOver = new MovingObjectPosition(en, (Vec3)o[1]);
        mc.pointedEntity = en;
        return true;
    }
    
    private Object[] zz(double zzD, final double zzE) {
        final Reach reach = (Reach) Raven.moduleManager.getModuleByClazz(Reach.class);
        final HitBox boxhit = (HitBox) Raven.moduleManager.getModuleByClazz(HitBox.class);
        if (reach != null && !isEnabled()) {
            zzD = (mc.playerController.extendedReach() ? 6.0 : 3.0);
        }
        final Entity entity1 = mc.getRenderViewEntity();
        Entity entity2 = null;
        if (entity1 == null) {
            return null;
        }
        mc.mcProfiler.startSection("pick");
        final Vec3 eyes_positions = entity1.getPositionEyes(1.0f);
        final Vec3 look = entity1.getLook(1.0f);
        final Vec3 new_eyes_pos = eyes_positions.addVector(look.xCoord * zzD, look.yCoord * zzD, look.zCoord * zzD);
        Vec3 zz6 = null;
        final List<Entity> zz7 = (List<Entity>)mc.theWorld.getEntitiesWithinAABBExcludingEntity(entity1, entity1.getEntityBoundingBox().addCoord(look.xCoord * zzD, look.yCoord * zzD, look.zCoord * zzD).expand(1.0, 1.0, 1.0));
        double zz8 = zzD;
        for (final Entity o : zz7) {
            if (o.canBeCollidedWith()) {
                final float ex = (float)(o.getCollisionBorderSize() * boxhit.exp(o));
                AxisAlignedBB zz9 = o.getEntityBoundingBox().expand((double)ex, (double)ex, (double)ex);
                zz9 = zz9.expand(zzE, zzE, zzE);
                final MovingObjectPosition zz10 = zz9.calculateIntercept(eyes_positions, new_eyes_pos);
                if (zz9.isVecInside(eyes_positions)) {
                    if (0.0 >= zz8 && zz8 != 0.0) {
                        continue;
                    }
                    entity2 = o;
                    zz6 = ((zz10 == null) ? eyes_positions : zz10.hitVec);
                    zz8 = 0.0;
                }
                else {
                    if (zz10 == null) {
                        continue;
                    }
                    final double zz11 = eyes_positions.distanceTo(zz10.hitVec);
                    if (zz11 >= zz8 && zz8 != 0.0) {
                        continue;
                    }
                    if (o == entity1.ridingEntity) {
                        if (zz8 != 0.0) {
                            continue;
                        }
                        entity2 = o;
                        zz6 = zz10.hitVec;
                    }
                    else {
                        entity2 = o;
                        zz6 = zz10.hitVec;
                        zz8 = zz11;
                    }
                }
            }
        }
        if (zz8 < zzD && !(entity2 instanceof EntityLivingBase) && !(entity2 instanceof EntityItemFrame)) {
            entity2 = null;
        }
        mc.mcProfiler.endSection();
        if (entity2 != null && zz6 != null) {
            return new Object[] { entity2, zz6 };
        }
        return null;
    }
}
