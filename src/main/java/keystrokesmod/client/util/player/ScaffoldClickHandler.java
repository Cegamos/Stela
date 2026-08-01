package keystrokesmod.client.util.player;

import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.util.IMinecraft;
import keystrokesmod.client.util.system.ReflectUtil;

public class ScaffoldClickHandler implements IMinecraft {
    private static float minCPS;
    private static float maxCPS;
    private static boolean initialized;
    private static final List<Integer> clickPattern = new LinkedList<>();
    private int cachedClicks;
    private static RandomisationMode randomisationMode;
    private static boolean clickOnMiss;
    private static boolean allowWrongPlace;

    public enum RandomisationMode {
        Stabilised,
        Even,
        Jitter
    }

    public static void initHandler(float minCPS, float maxCPS, RandomisationMode randomisationMode, boolean allowWrongPlace, boolean clickOnMiss) {
        ScaffoldClickHandler.minCPS = minCPS;
        ScaffoldClickHandler.maxCPS = maxCPS;
        ScaffoldClickHandler.randomisationMode = randomisationMode;
        ScaffoldClickHandler.clickOnMiss = clickOnMiss;
        ScaffoldClickHandler.allowWrongPlace = allowWrongPlace;

        initialized = true;
    }

    private void generateClickPattern() {
        clickPattern.clear();
        int totalTicks = 20;

        double clicks = minCPS + ThreadLocalRandom.current().nextDouble() * (maxCPS - minCPS);

        for (int i = 0; i < totalTicks; i++) {
            clickPattern.add(0);
        }

        int cycles = clicks / 20 <= 1 ? 1 : 2;

        for (int j = 0; j < cycles; j++) {
            double normClicks = Math.min(clicks, 20);

            switch (randomisationMode) {
                case Stabilised: {
                    double interval = (double) totalTicks / normClicks;
                    int remainder = (int) (totalTicks % normClicks);

                    int currentIndex = 0;

                    for (int i = 0; i < normClicks; i++) {
                        int index = currentIndex % totalTicks;
                        clickPattern.set(index, clickPattern.get(index) + 1);
                        currentIndex += (int) Math.max(interval, 1);
                        if (remainder > 0) {
                            currentIndex++;
                            remainder--;
                        }
                    }
                    break;
                }
                case Even: {
                    double interval = (double) totalTicks / normClicks;
                    for (int i = 0; i < clicks; i++) {
                        int index = (int) Math.round(i * interval) % totalTicks;
                        clickPattern.set(index, clickPattern.get(index) + 1);
                    }
                    break;
                }
                case Jitter: {
                    double clicksToDistribute = normClicks;

                    for (int i = 0; i < totalTicks; i++) {
                        double probability = clicksToDistribute / (totalTicks - i);
                        if (ThreadLocalRandom.current().nextDouble() < probability) {
                            clickPattern.set(i, clickPattern.get(i) + 1);
                            clicksToDistribute--;
                        }
                    }
                    break;
                }
            }

            clicks -= 20;
        }
    }

    @EventLink
    public final Listener<PreTickEvent> onTick = e -> {
        if (mc.thePlayer == null || mc.theWorld == null) {
            return;
        }

        if (initialized && mc.currentScreen == null) {
            if (clickPattern.isEmpty()) {
                generateClickPattern();
            }

            if (!clickPattern.isEmpty()) {
                Integer next = clickPattern.remove(0);
                if (next != null) {
                    cachedClicks += next;
                }
            }
            
            for (int i = 0; i < cachedClicks; i++) {
                if (ReflectUtil.getRightClickDelayTimer() == 0) {
                    ReflectUtil.rightClickMouse();
                }
            }
        }
        cachedClicks = 0;
        initialized = false;
    };
}
