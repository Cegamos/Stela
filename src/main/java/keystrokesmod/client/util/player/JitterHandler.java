package keystrokesmod.client.util.player;

import java.util.ArrayList;
import java.util.List;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PacketSendEvent;
import keystrokesmod.client.util.IMinecraft;
import net.minecraft.network.play.client.C0APacketAnimation;

public class JitterHandler implements IMinecraft {
    private static final List<Long> clicks = new ArrayList<>();
    private static int lastTicksExisted;

    private static float tremorYaw = 0;
    private static float tremorPitch = 0;

    private static float targetYaw = 0;
    private static float targetPitch = 0;

    private static float clickImpulseYaw;
    private static float clickImpulsePitch;

    private static float jitterYaw;
    private static float jitterPitch;

    public static float[] calculateJitter(float strength, boolean interpolate) {
        long time = System.currentTimeMillis();
        clicks.removeIf(aLong -> aLong + 1000L < time);

        if (clicks.isEmpty()) {
            return new float[]{jitterYaw, jitterPitch};
        }

        boolean lastTickClicked = false;

        for (Long clickTime : clicks) {
            if (clickTime + (long) 16.67 > time) {
                lastTickClicked = true;
                break;
            }
        }

        if (lastTickClicked) {
            clickImpulseYaw += randomizeFloat(-strength, strength);
            clickImpulsePitch += randomizeFloat(-strength, strength);
        }

        float cpsFactor = Math.min(clicks.size() / 12f, 1.5f);

        float yawJitter = tremorYaw * 0.6f * cpsFactor + clickImpulseYaw;
        float pitchJitter = tremorPitch * 0.6f * cpsFactor + clickImpulsePitch;

        if (interpolate) {
            jitterYaw = interpolateFloat(jitterYaw, yawJitter, 0.25F);
            jitterPitch = interpolateFloat(jitterPitch, pitchJitter, 0.25F);
        } else {
            jitterYaw = yawJitter;
            jitterPitch = pitchJitter;
        }

        if (mc.thePlayer != null && mc.thePlayer.ticksExisted != lastTicksExisted) {
            if (Math.random() < 0.08) {
                targetYaw = randomizeFloat(-0.15f, 0.15f);
                targetPitch = randomizeFloat(-0.15f, 0.15f);
            }

            tremorYaw += (targetYaw - tremorYaw) * 0.12f;
            tremorPitch += (targetPitch - tremorPitch) * 0.12f;

            clickImpulseYaw *= 0.75f;
            clickImpulsePitch *= 0.75f;
            lastTicksExisted = mc.thePlayer.ticksExisted;
        }

        return new float[]{jitterYaw, jitterPitch};
    }

    private static float randomizeFloat(float min, float max) {
        return min + (float) Math.random() * (max - min);
    }

    private static float interpolateFloat(float start, float end, float factor) {
        return start + factor * (end - start);
    }

    @EventLink
    public final Listener<PacketSendEvent> onPacketSend = e -> {
        if (e.getPacket() instanceof C0APacketAnimation) {
            clicks.add(System.currentTimeMillis());
        }
    };
}
