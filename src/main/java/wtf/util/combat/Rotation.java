package wtf.util.combat;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class Rotation {

    private float yaw, pitch;

    public Rotation(float yaw, float pitch) {
        this.yaw = yaw;
        this.pitch = pitch;
    }

    public Rotation(EntityPlayerSP player) {
        this(player.rotationYaw, player.rotationPitch);
    }

    public Rotation(Rotation rotation) {
        this(rotation.getYaw(), rotation.getPitch());
    }

    public Rotation(Vec3 from, Vec3 to) {
        final Vec3 diff = to.subtract(from);
        this.yaw = wrapDegrees((float) Math.toDegrees(Math.atan2(diff.zCoord, diff.xCoord)) - 90F);
        this.pitch = wrapDegrees((float) (-Math.toDegrees(Math.atan2(diff.yCoord, Math.sqrt(diff.xCoord * diff.xCoord + diff.zCoord * diff.zCoord)))));
    }

    public Vec3 forwardVector() {
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        return new Vec3(Math.sin(-yawRad) * Math.cos(pitchRad), -Math.sin(pitchRad), Math.cos(-yawRad) * Math.cos(pitchRad));
    }

    public void limit(Rotation to, float limit) {
        setYaw(updateAngle(getYaw(), to.getYaw(), limit));
        setPitch(updateAngle(getPitch(), to.getPitch(), limit));
    }

    public Vec3 asVec() {
        float f = MathHelper.cos(-this.getYaw() * 0.017453292F - (float) Math.PI);
        float f1 = MathHelper.sin(-this.getYaw() * 0.017453292F - (float) Math.PI);
        float f2 = -MathHelper.cos(-this.getPitch() * 0.017453292F);
        float f3 = MathHelper.sin(-this.getPitch() * 0.017453292F);
        return new Vec3(f1 * f2, f3, f * f2);
    }

    public void limitLastYaw(Rotation last, float speed) {
        setYaw(updateAngle(last.getYaw(), getYaw(), speed));
    }

    public void limitLastPitch(Rotation last, float speed) {
        setPitch(updateAngle(last.getPitch(), getPitch(), speed));
    }

    public void setRotation(Rotation rotation) {
        setYaw(rotation.getYaw());
        setPitch(rotation.getPitch());
    }

    public void setRotation(EntityPlayerSP entity) {
        setYaw(entity.rotationYaw);
        setPitch(entity.rotationPitch);
    }

    public void setYaw(float yaw) {
        this.yaw = yaw;
    }

    public void setPitch(float pitch) {
        this.pitch = pitch;
    }

    public float getYaw() {
        return this.yaw;
    }

    public float getPitch() {
        return this.pitch;
    }
    
    private float wrapDegrees(float value) {
        float mod = value % 360.0F;
        if (mod >= 180.0F) {
            mod -= 360.0F;
        }

        if (mod < -180.0F) {
            mod += 360.0F;
        }

        return mod;
    }

    private float updateAngle(float origin, float next, float speed) {
        float f = MathHelper.wrapAngleTo180_float(next - origin);
        if (f > speed) f = speed;
        if (f < -speed) f = -speed;
        return origin + f;
    }
}
