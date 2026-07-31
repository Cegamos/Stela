package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.MotionEvent;

import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Shadow;
import keystrokesmod.client.stela.annotations.Target;

import com.mojang.authlib.GameProfile;

import keystrokesmod.client.module.modules.movement.NoSlow;
import keystrokesmod.client.module.modules.movement.Sprint;
import net.minecraft.client.Minecraft;
import net.minecraft.client.audio.PositionedSoundRecord;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.potion.Potion;
import net.minecraft.util.MovementInput;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

@Mixin(EntityPlayerSP.class)
public abstract class MixinEntityPlayerSP extends AbstractClientPlayer {

    public MixinEntityPlayerSP(World p_i45074_1_, GameProfile p_i45074_2_) {
        super(p_i45074_1_, p_i45074_2_);
    }

    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("HEAD"))
    private void onPreMotion() {
        EventBus.INSTANCE.post(new MotionEvent(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround, MotionEvent.Phase.PRE));
    }

    @Inject(method = "onUpdateWalkingPlayer", desc = "()V", target = @Target("TAIL"))
    private void onPostMotion() {
        EventBus.INSTANCE.post(new MotionEvent(this.posX, this.getEntityBoundingBox().minY, this.posZ, this.rotationYaw, this.rotationPitch, this.onGround, MotionEvent.Phase.POST));
    }
}
