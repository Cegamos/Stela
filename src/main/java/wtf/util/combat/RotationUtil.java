package wtf.util.combat;

import net.minecraft.util.MathHelper;
import wtf.util.Wrapper;

public final class RotationUtil extends Wrapper {
    
    private static float lastSensitivity = -1.0f;
    private static float cachedStepSize = -1.0f;

    public static Rotation mouseSens(final Rotation target, final Rotation prev) {
        if (target.getYaw() == prev.getYaw() && target.getPitch() == prev.getPitch()) {
            return target;
        }

        final float stepSize = getCachedStepSize();

        float deltaYaw = MathHelper.wrapAngleTo180_float(target.getYaw() - prev.getYaw());
        float deltaPitch = MathHelper.wrapAngleTo180_float(target.getPitch() - prev.getPitch());

        long stepsX = Math.round(deltaYaw / stepSize);
        long stepsY = Math.round(deltaPitch / stepSize);

        float fixedYaw = prev.getYaw() + stepsX * stepSize;
        float fixedPitch = MathHelper.clamp_float(prev.getPitch() + stepsY * stepSize, -90.0f, 90.0f);

        return new Rotation(fixedYaw, fixedPitch);
    }

    public static void applyMouseSensTarget(Rotation target, final Rotation prev) {
        if (target.getYaw() == prev.getYaw() && target.getPitch() == prev.getPitch()) {
            return;
        }

        final float stepSize = getCachedStepSize();

        float deltaYaw = MathHelper.wrapAngleTo180_float(target.getYaw() - prev.getYaw());
        float deltaPitch = MathHelper.wrapAngleTo180_float(target.getPitch() - prev.getPitch());

        long stepsX = Math.round(deltaYaw / stepSize);
        long stepsY = Math.round(deltaPitch / stepSize);

        target.setYaw(prev.getYaw() + stepsX * stepSize);
        target.setPitch(MathHelper.clamp_float(prev.getPitch() + stepsY * stepSize, -90.0f, 90.0f));
    }

    private static float getCachedStepSize() {
        float currentSens = mc.gameSettings.mouseSensitivity;
        
        if (currentSens != lastSensitivity) {
            lastSensitivity = currentSens;
            
            float sens = currentSens;
            if (sens == 0.5f) {
                sens = 0.47887325f;
            }
            
            final float f1 = sens * 0.6f + 0.2f;
            cachedStepSize = f1 * f1 * f1 * 1.2f;
        }
        
        return cachedStepSize;
    }
}