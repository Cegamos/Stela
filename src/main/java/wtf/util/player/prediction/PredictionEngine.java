package wtf.util.player.prediction;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import wtf.util.IMinecraft;
import wtf.util.timing.Clock;

public class PredictionEngine implements IMinecraft {
    private float currentTargetOffset;
    private AxisAlignedBB lastTrackedAABB;
    private int ticksExisted;
    public int currentReactionTime;
    public final List<Double> previousTargetMotions = new ArrayList<>();
    public boolean lastReset;
    private final Random rand = new Random();
    private final Clock updateTimer = new Clock(0);
    private Vec3 lastTrackedMoveDelta;

    public AxisAlignedBB simulatePredictions(EntityLivingBase currentTarget, float attackRange, boolean simulateReactionTime, float[] reactionTime) {
        if (currentTarget == null || mc.thePlayer == null) return null;
        
        lastReset = false;
        double dist = mc.thePlayer.getDistanceToEntity(currentTarget);
        Vec3 prediction;
        AxisAlignedBB hitbox = currentTarget.getEntityBoundingBox();

        boolean flag = false;
        double speed = Math.hypot(currentTarget.posX - currentTarget.prevPosX, currentTarget.posZ - currentTarget.prevPosZ);

        if (dist > attackRange) {
            currentTargetOffset = (float) (Math.min(Math.max((dist - attackRange) * 3, 0), 8));
            flag = true;
        }

        if (speed > 0.4) {
            double extra = Math.random() * (0.9 + rand.nextFloat() * 0.2);
            float target = (float) (-Math.min(Math.max(dist, 0), 8) + (rand.nextBoolean() ? extra : -extra));
            currentTargetOffset = currentTargetOffset + 0.05f * (target - currentTargetOffset);
            flag = true;
        }

        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(currentTarget.getEntityBoundingBox())) {
            currentTargetOffset = 3;
            flag = true;
        }

        Vec3 delta = new Vec3(currentTarget.posX - currentTarget.prevPosX, 0, currentTarget.posZ - currentTarget.prevPosZ);
        double mult = flag ? currentTargetOffset : 0;
        prediction = new Vec3(delta.xCoord * mult, 0, delta.zCoord * mult);

        if (simulateReactionTime) {
            if (lastTrackedAABB == null) {
                lastTrackedAABB = hitbox;
            }

            if (mc.thePlayer.ticksExisted != ticksExisted) {
                previousTargetMotions.add(new Vec3(currentTarget.posX - currentTarget.prevPosX, currentTarget.posY - currentTarget.prevPosY, currentTarget.posZ - currentTarget.prevPosZ).lengthVector());

                if (updateTimer.finished(currentReactionTime * 50L + 50L)) {
                    lastTrackedAABB = hitbox;
                    lastTrackedMoveDelta = new Vec3(currentTarget.posX - currentTarget.prevPosX, currentTarget.posY - currentTarget.prevPosY, currentTarget.posZ - currentTarget.prevPosZ);

                    double averageMotion = 0;
                    for (Double motion : previousTargetMotions) {
                        averageMotion += motion;
                    }
                    if (!previousTargetMotions.isEmpty()) {
                        averageMotion /= previousTargetMotions.size();
                    }
                    double motionPercentage = MathHelper.clamp_double(averageMotion * 2, 0, 1);

                    currentReactionTime = (int) (reactionTime[0] + motionPercentage * (reactionTime[1] - reactionTime[0]));
                    updateTimer.start();
                } else if (lastTrackedMoveDelta != null) {
                    lastTrackedAABB = lastTrackedAABB.offset(lastTrackedMoveDelta.xCoord, lastTrackedMoveDelta.yCoord, lastTrackedMoveDelta.zCoord);
                    hitbox = lastTrackedAABB;
                }

                while (previousTargetMotions.size() > 20) {
                    previousTargetMotions.remove(0);
                }
            }
        }

        ticksExisted = mc.thePlayer.ticksExisted;
        return hitbox.offset(prediction.xCoord, prediction.yCoord, prediction.zCoord);
    }

    public void reset() {
        lastTrackedAABB = null;
        currentReactionTime = 0;
        previousTargetMotions.clear();
        lastReset = true;
    }
}
