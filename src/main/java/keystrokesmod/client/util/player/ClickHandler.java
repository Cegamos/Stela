package keystrokesmod.client.util.player;

import java.util.Arrays;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.MouseStateUpdateEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.math.PerlinNoise;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.network.play.client.C02PacketUseEntity;

public class ClickHandler implements IMinecraft {
    private static boolean rayTrace;
    private static float cps;
    private static float attackRange;
    private static EntityLivingBase target;
    private static boolean initialized;
    private static boolean failSwing;
    private static ClickMode clickMode = ClickMode.PlayerController;
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

    private static final int TICKS = 20;
    private static final int[] clickPattern = new int[TICKS];
    private static final int[] cyclePattern = new int[TICKS];
    private static final int[] randomSlots = new int[TICKS];
    private static final int[] backupPattern = new int[TICKS];
    private static int currentTickIndex = TICKS; 

    public enum ClickMode {
        Legit, Packet, PlayerController
    }

    public static void initHandler(float cps, double perlinRandAdd, boolean rayTrace, boolean failSwing,
            boolean respectHitDelay, int maxDelay, float attackRange, boolean doubleClick, float maxClickAmount,
            int maxSkipsInARow, int maxConcentrationDiff, float consistency, ClickMode clickMode,
            EntityLivingBase target) {
        initHandler(cps, perlinRandAdd, rayTrace, failSwing, respectHitDelay, maxDelay, attackRange, doubleClick,
                maxClickAmount, maxSkipsInARow, maxConcentrationDiff, consistency, clickMode, target, 0.0f);
    }

    private static long lastClickTime;

    public static void initHandler(float cps, double perlinRandAdd, boolean rayTrace, boolean failSwing,
            boolean respectHitDelay, int maxDelay, float attackRange, boolean doubleClick, float maxClickAmount,
            int maxSkipsInARow, int maxConcentrationDiff, float consistency, ClickMode clickMode,
            EntityLivingBase target, float jitterStrength) {
        ClickHandler.cps = cps;
        ClickHandler.rayTrace = rayTrace;
        ClickHandler.attackRange = attackRange;
        ClickHandler.target = target;
        ClickHandler.failSwing = failSwing;
        ClickHandler.clickMode = clickMode;
        ClickHandler.respectHitDelay = respectHitDelay;
        ClickHandler.maxDelay = maxDelay;
        ClickHandler.doubleClick = doubleClick;
        ClickHandler.maxClickAmount = maxClickAmount;
        ClickHandler.maxSkipsInARow = maxSkipsInARow;
        ClickHandler.maxConcentrationDiff = maxConcentrationDiff;
        ClickHandler.consistency = consistency;
        ClickHandler.perlinRandAdd = perlinRandAdd;
        ClickHandler.jitterStrength = jitterStrength;

        lastClickTime = System.currentTimeMillis();
        initialized = true;
    }

    private void generateBasePattern(double clicks, int ticks) {
        Arrays.fill(clickPattern, 0);

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
            Arrays.fill(cyclePattern, 0);
            
            int slotsCount = 0;
            double normClicks = Math.min(clicks, 20);
            double clicksToDistribute = normClicks;

            for (int i = 0; i < ticks && clicksToDistribute > 0; i++) {
                double probability = clicksToDistribute / (ticks - i);
                if (ThreadLocalRandom.current().nextDouble() < probability) {
                    randomSlots[slotsCount++] = i;
                    clicksToDistribute--;
                }
            }

            double interval = (double) ticks / normClicks;

            for (int i = 0; i < slotsCount; i++) {
                double blended = randomSlots[i] * (1.0 - consistency)
                        + (int) Math.round(i * interval) * consistency;
                int index = (int) Math.round(blended);
                index = Math.max(0, Math.min(ticks - 1, index));

                int attempts = 0;
                while (cyclePattern[index] > 0 && attempts < ticks) {
                    index = (index + 1) % ticks;
                    attempts++;
                }

                cyclePattern[index]++;
            }

            for (int i = 0; i < ticks; i++) {
                clickPattern[i] += cyclePattern[i];
            }

            clicks -= 20;
        }

        if (doubleClick) {
            for (int j = 0; j < maxClickAmount; j++) {
                int from = ThreadLocalRandom.current().nextInt(ticks);
                if (clickPattern[from] == 0)
                    continue;

                int to = (int) Math.min(ticks - 1, Math.max(0, from + (rand.nextInt(3) - 1)));
                if (from == to)
                    continue;

                clickPattern[from]--;
                clickPattern[to]++;
            }
        }
    }

    private void generateClickPattern() {
        generateBasePattern(cps, TICKS);

        int skipsInARow = 0;

        for (int i = 0; i < TICKS; i++) {
            int click = clickPattern[i];

            if (click == 0) {
                skipsInARow++;
            } else {
                skipsInARow = 0;
            }

            if (skipsInARow > maxSkipsInARow) {
                if (i + 1 < TICKS - 1) {
                    int nextClick = clickPattern[i + 1];

                    if (nextClick != 0) {
                        clickPattern[i] = nextClick;
                        clickPattern[i + 1] = 0;
                    }
                }
            }
        }

        int concentration = maxRunLength();

        if (cps == lastCPS) {
            int i = 0;
            System.arraycopy(clickPattern, 0, backupPattern, 0, TICKS);

            while (Math.abs(concentration - lastConcentration) > maxConcentrationDiff && i < 10) {
                generateBasePattern(cps, TICKS);
                concentration = maxRunLength();
                i++;
            }
            if (i >= 10) {
                System.arraycopy(backupPattern, 0, clickPattern, 0, TICKS);
            }
        }

        lastConcentration = concentration;
        lastCPS = (int) cps;
    }

    private int maxRunLength() {
        int maxRun = 1;
        int currentRun = 1;

        for (int i = 1; i < TICKS; i++) {
            if (clickPattern[i] == clickPattern[i - 1]) {
                currentRun++;
                maxRun = Math.max(maxRun, currentRun);
            } else {
                currentRun = 1;
            }
        }
        return maxRun;
    }

    @EventLink
    public final Listener<PreTickEvent> staticTick = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (target != null && initialized && mc.currentScreen == null) {
            if (currentTickIndex >= TICKS) {
                generateClickPattern();
                currentTickIndex = 0;
            }

            cachedClicks += clickPattern[currentTickIndex++];
        } else {
            cachedClicks = 0;
            currentTickIndex = TICKS;
        }
    };

    @EventLink
    public final Listener<MouseStateUpdateEvent> mouseStateUpdate = event -> {
        leftClickCounter = Math.max(0, leftClickCounter - 1);
        clickingNow = false;
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
            if (System.currentTimeMillis() - lastClickTime > 200) {
                initialized = false;
            }
        } else if (initialized) {
            if (cachedClicks > 0 && mc.currentScreen == null) {
                ReflectUtil.setLeftClickCounter(0);
                for (int i = 0; i < cachedClicks; i++) {
                    attack();
                }
                clickingNow = true;
            }
            cachedClicks = 0;
            if (System.currentTimeMillis() - lastClickTime > 200) {
                initialized = false;
            }
        } else {
            clickingNow = false;
        }
    };

    private static boolean hasHitDelayPassed() {
        return !respectHitDelay || leftClickCounter <= 0;
    }

    private void handleFailSwing() {
        if (itemUseAttack() && mc.currentScreen == null) {
            switch (clickMode) {
                case Legit:
                	ReflectUtil.clickMouse();
                    break;
			default:
				break;
            }
        }

        leftClickCounter = maxDelay;
    }

    public static void attack() {
        if (itemUseAttack() && mc.currentScreen == null) {
            switch (clickMode) {
                case Legit:
                    ReflectUtil.clickMouse();
                    break;
			default:
				break;
            }
        }
    }

    private static boolean lastUsingItem;
    private static int releaseTicks = 0;

    private static boolean itemUseAttack() {
 
        boolean usingItem = mc.thePlayer.isUsingItem();
        boolean released = !usingItem && lastUsingItem;
        lastUsingItem = usingItem;

        if (released) {
            releaseTicks = 2;
        }

        if (releaseTicks > 0) {
            releaseTicks--;
            return false;
        }

        return !usingItem;
    }

}