package keystrokesmod.client.util.player.prediction;

import keystrokesmod.client.util.IMinecraft;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Vec3;

public class SimulationEngine implements IMinecraft {

    public static Vec3 predictPlayerPosition(int ticksAhead) {
        if (mc.thePlayer == null) return new Vec3(0, 0, 0);
        
        double motionX = mc.thePlayer.posX - mc.thePlayer.prevPosX;
        double motionY = mc.thePlayer.posY - mc.thePlayer.prevPosY;
        double motionZ = mc.thePlayer.posZ - mc.thePlayer.prevPosZ;

        double predictedX = mc.thePlayer.posX;
        double predictedY = mc.thePlayer.posY;
        double predictedZ = mc.thePlayer.posZ;

        for (int i = 0; i < ticksAhead; i++) {
            predictedX += motionX;
            predictedY += motionY;
            predictedZ += motionZ;
            motionX *= 0.98;
            motionZ *= 0.98;
            motionY = (motionY - 0.08) * 0.98;
        }

        return new Vec3(predictedX, predictedY, predictedZ);
    }

    public static Vec3 predictTargetPosition(EntityLivingBase target, int ticksAhead) {
        if (target == null) return new Vec3(0, 0, 0);

        double motionX = target.posX - target.prevPosX;
        double motionY = target.posY - target.prevPosY;
        double motionZ = target.posZ - target.prevPosZ;

        double predictedX = target.posX;
        double predictedY = target.posY;
        double predictedZ = target.posZ;

        for (int i = 0; i < ticksAhead; i++) {
            predictedX += motionX;
            predictedY += motionY;
            predictedZ += motionZ;
            motionX *= 0.98;
            motionZ *= 0.98;
            motionY = (motionY - 0.08) * 0.98;
        }

        return new Vec3(predictedX, predictedY, predictedZ);
    }
}
