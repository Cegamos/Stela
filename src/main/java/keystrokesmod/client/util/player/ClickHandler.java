package keystrokesmod.client.util.player;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.MouseStateUpdateEvent;
import keystrokesmod.client.event.impl.StaticTickEvent;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.math.PerlinNoise;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;

public class ClickHandler implements IMinecraft {
    private static boolean rayTrace;
    private static float cps;
    private static float attackRange;
    private static boolean ignoreItemUse;
    private static EntityLivingBase target;
    private static boolean initialized;
    private static boolean failSwing;
    private static ClickMode clickMode = ClickMode.PlayerController;
    private static List<Integer> clickPattern = new LinkedList<>();
    private static float swingRange;
    public static boolean clickingNow;
    private int cachedClicks;
    private static boolean respectHitDelay;
    private static int maxDelay;
    private static boolean doubleClick;
    private static int maxSkipsInARow;
    private double lastConcentration;
    private static int maxConcentrationDiff;
    private static int lastCPS;
    private static float consistency;
    private static float maxClickAmount;
    private static int leftClickCounter;
    private int cycles;
    private static double perlinRandAdd;
    private static float jitterStrength = 0.0f;
    private static final PerlinNoise perlin = new PerlinNoise();
    private static final Random rand = new Random();

    public enum ClickMode {
        Legit,
        Packet,
        PlayerController
    }

    public static void initHandler(float cps,
                                   double perlinRandAdd,
                                   boolean rayTrace,
                                   boolean ignoreItemUse,
                                   boolean failSwing,
                                   boolean respectHitDelay,
                                   int maxDelay,
                                   float attackRange,
                                   float swingRange,
                                   boolean doubleClick,
                                   float maxClickAmount,
                                   int maxSkipsInARow,
                                   int maxConcentrationDiff,
                                   float consistency,
                                   ClickMode clickMode,
                                   EntityLivingBase target) {
        initHandler(cps, perlinRandAdd, rayTrace, ignoreItemUse, failSwing, respectHitDelay, maxDelay, attackRange, swingRange, doubleClick, maxClickAmount, maxSkipsInARow, maxConcentrationDiff, consistency, clickMode, target, 0.0f);
    }

    public static void initHandler(float cps,
                                   double perlinRandAdd,
                                   boolean rayTrace,
                                   boolean ignoreItemUse,
                                   boolean failSwing,
                                   boolean respectHitDelay,
                                   int maxDelay,
                                   float attackRange,
                                   float swingRange,
                                   boolean doubleClick,
                                   float maxClickAmount,
                                   int maxSkipsInARow,
                                   int maxConcentrationDiff,
                                   float consistency,
                                   ClickMode clickMode,
                                   EntityLivingBase target,
                                   float jitterStrength) {
        ClickHandler.cps = cps;
        ClickHandler.rayTrace = rayTrace;
        ClickHandler.attackRange = attackRange;
        ClickHandler.target = target;
        ClickHandler.ignoreItemUse = ignoreItemUse;
        ClickHandler.failSwing = failSwing;
        ClickHandler.clickMode = clickMode;
        ClickHandler.swingRange = swingRange;
        ClickHandler.respectHitDelay = respectHitDelay;
        ClickHandler.maxDelay = maxDelay;
        ClickHandler.doubleClick = doubleClick;
        ClickHandler.maxClickAmount = maxClickAmount;
        ClickHandler.maxSkipsInARow = maxSkipsInARow;
        ClickHandler.maxConcentrationDiff = maxConcentrationDiff;
        ClickHandler.consistency = consistency;
        ClickHandler.perlinRandAdd = perlinRandAdd;
        ClickHandler.jitterStrength = jitterStrength;

        initialized = true;
    }

    private void generateBasePattern(double clicks, int ticks) {
        clickPattern.clear();

        for (int i = 0; i < ticks; i++) {
            clickPattern.add(0);
        }

        if (cycles == 10) {
            perlin.setSeed(rand.nextGaussian() * 255);
            cycles = 0;
        }

        if (mc.thePlayer != null) {
            clicks += perlin.noise(mc.thePlayer.ticksExisted) * perlinRandAdd;
        }

        cycles++;
        int cyclesVal = (int) Math.ceil(clicks / ticks);

        for (int j = 0; j < cyclesVal; j++) {
            List<Integer> cyclePattern = new ArrayList<>();
            for (int i = 0; i < ticks; i++) {
                cyclePattern.add(0);
            }

            List<Integer> randomSlots = new ArrayList<>();
            double normClicks = Math.min(clicks, 20);
            double clicksToDistribute = normClicks;

            for (int i = 0; i < ticks && clicksToDistribute > 0; i++) {
                double probability = clicksToDistribute / (ticks - i);
                if (ThreadLocalRandom.current().nextDouble() < probability) {
                    randomSlots.add(i);
                    clicksToDistribute--;
                }
            }

            double interval = (double) ticks / normClicks;

            for (int i = 0; i < randomSlots.size(); i++) {
                double blended = randomSlots.get(i) * (1.0 - consistency) + (int) Math.round(i * interval) * consistency;
                int index = (int) Math.round(blended);
                index = Math.max(0, Math.min(ticks - 1, index));

                int attempts = 0;
                while (cyclePattern.get(index) > 0 && attempts < ticks) {
                    index = (index + 1) % ticks;
                    attempts++;
                }

                cyclePattern.set(index, cyclePattern.get(index) + 1);
            }

            for (int i = 0; i < ticks; i++) {
                clickPattern.set(i, clickPattern.get(i) + cyclePattern.get(i));
            }

            clicks -= 20;
        }

        if (doubleClick) {
            for (int j = 0; j < maxClickAmount; j++) {
                int from = ThreadLocalRandom.current().nextInt(ticks);
                if (clickPattern.get(from) == 0) continue;

                int to = (int) Math.min(ticks - 1, Math.max(0, from + (rand.nextInt(3) - 1)));
                if (from == to) continue;

                clickPattern.set(from, clickPattern.get(from) - 1);
                clickPattern.set(to, clickPattern.get(to) + 1);
            }
        }
    }

    private void generateClickPattern() {
        generateBasePattern(cps, 20);

        int skipsInARow = 0;

        for (int i = 0; i < clickPattern.size(); i++) {
            int click = clickPattern.get(i);

            if (click == 0) {
                skipsInARow++;
            } else {
                skipsInARow = 0;
            }

            if (skipsInARow > maxSkipsInARow) {
                if (i + 1 < clickPattern.size() - 1) {
                    int nextClick = clickPattern.get(i + 1);

                    if (nextClick != 0) {
                        clickPattern.set(i, nextClick);
                        clickPattern.set(i + 1, 0);
                    }
                }
            }
        }

        int concentration = maxRunLength();

        if (cps == lastCPS) {
            int i = 0;
            List<Integer> originalPattern = clickPattern;

            while (Math.abs(concentration - lastConcentration) > maxConcentrationDiff && i < 10) {
                generateBasePattern(cps, 20);
                concentration = maxRunLength();
                i++;
            }
            if (i >= 10) {
                clickPattern = originalPattern;
            }
        }

        lastConcentration = concentration;
        lastCPS = (int) cps;
    }

    private int maxRunLength() {
        int maxRun = 1;
        int currentRun = 1;

        for (int i = 1; i < clickPattern.size(); i++) {
            if (clickPattern.get(i).equals(clickPattern.get(i - 1))) {
                currentRun++;
                maxRun = Math.max(maxRun, currentRun);
            } else {
                currentRun = 1;
            }
        }
        return maxRun;
    }

    @EventLink
    public final Listener<StaticTickEvent> staticTick = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (target != null && initialized && mc.currentScreen == null) {
            if (clickPattern.isEmpty()) {
                generateClickPattern();
            }

            Integer next = clickPattern.get(0);
            clickPattern.remove(0);

            if (next != null) {
                cachedClicks += next;
            }
        } else {
            cachedClicks = 0;
        }
    };
    
    @EventLink
    public final Listener<MouseStateUpdateEvent> mouseStateUpdate = event -> {
        leftClickCounter = Math.max(0, leftClickCounter - 1);
        clickingNow = false;
        finalizeHandler();
    };

    private void finalizeHandler() {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (jitterStrength > 0.0f && cachedClicks > 0 && mc.thePlayer != null) {
            float[] jitter = JitterHandler.calculateJitter(jitterStrength, true);
            mc.thePlayer.rotationYaw += jitter[0];
            mc.thePlayer.rotationPitch += jitter[1];
        }

        if (target != null && initialized) {
            double distance = mc.thePlayer.getDistanceToEntity(target);
            boolean rayTraceFailed = (rayTrace && (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null));
            boolean shouldAttack = distance <= attackRange && !rayTraceFailed;
            boolean willAttack = hasHitDelayPassed() && shouldAttack && cachedClicks > 0;

            if (cachedClicks > 0 && willAttack) {
                boolean shouldFailSwing = failSwing && (distance > attackRange || rayTraceFailed);

                if (hasHitDelayPassed()) {
                    for (int i = 0; i < cachedClicks; i++) {
                        if (shouldAttack) {
                            attack();
                        } else if (shouldFailSwing) {
                            handleFailSwing();
                        }
                    }
                    clickingNow = shouldAttack || shouldFailSwing;
                }
            } else if (mc.currentScreen == null && mc.inGameHasFocus) {
                leftClickCounter = 0;
            }

            cachedClicks = 0;
            initialized = false;
        } else if (initialized) {
            if (cachedClicks > 0 && mc.currentScreen == null) {
                for (int i = 0; i < cachedClicks; i++) {
                    ReflectUtil.clickMouse();
                }
                clickingNow = true;
            }
            cachedClicks = 0;
            initialized = false;
        } else {
            clickingNow = false;
        }
    }

    private static boolean hasHitDelayPassed() {
        return !respectHitDelay || leftClickCounter <= 0;
    }

    private void handleFailSwing() {
        if (mc.currentScreen == null) {
            ReflectUtil.clickMouse();
        }
        leftClickCounter = maxDelay;
    }

    public static void attack() {
        if (mc.currentScreen == null) {
            switch (clickMode) {
                case Legit:
                    ReflectUtil.clickMouse();
                    break;
                case Packet:
                    mc.thePlayer.swingItem();
                    if (mc.getNetHandler() != null && target != null) {
                        mc.getNetHandler().addToSendQueue(new C02PacketUseEntity(target, C02PacketUseEntity.Action.ATTACK));
                    }
                    break;
                case PlayerController:
                    if (mc.playerController != null && target != null) {
                        mc.playerController.attackEntity(mc.thePlayer, target);
                        mc.thePlayer.swingItem();
                    }
                    break;
            }
        }
    }
}
