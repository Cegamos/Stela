package keystrokesmod.client.module.modules.combat;

import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PreRenderTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemSword;

@ModuleInfo(name = "AutoBlock", category = Category.Combat)
public class AutoBlock extends Mod {
    public RangeValue duration = new RangeValue("Block duration (MS)", this, 20.0, 100.0, 1.0, 500.0, 1.0);
    public RangeValue distance = new RangeValue("Distance to player (blocks)", this, 0.0, 3.0, 0.0, 6.0, 0.01);
    public NumberValue chance = new NumberValue("Chance %", this, 100.0, 0.0, 100.0, 1.0);
    
    private boolean engaged;
    private final Clock engagedTime = new Clock(0);

    @EventLink
    private final Listener<PreRenderTickEvent> preRenderTick = event -> both();

    @EventLink
    private final Listener<PostRenderTickEvent> postRenderTick = event -> both();

    private void both() {
        if (mc.thePlayer == null || mc.currentScreen != null) return;

        if (this.engaged) {
            // Liberar si el tiempo terminó o el usuario soltó el clic
            if ((this.engagedTime.hasFinished() || !Mouse.isButtonDown(0)) && 
                this.engagedTime.getElapsedTime() >= duration.getInputMin()) {
                this.engaged = false;
                release();
            }
            return;
        }

        if (!Mouse.isButtonDown(0)) return;

        if (mc.thePlayer.getCurrentEquippedItem() == null || 
          !(mc.thePlayer.getCurrentEquippedItem().getItem() instanceof ItemSword)) {
            return;
        }

        if (mc.objectMouseOver == null) return;
        final Entity target = mc.objectMouseOver.entityHit;
        if (target == null) return;

        final double distSq = mc.thePlayer.getDistanceSqToEntity(target);
        final double minD = distance.getInputMin();
        final double maxD = distance.getInputMax();
        
        if (distSq < (minD * minD) || distSq > (maxD * maxD)) return;

        final double chanceVal = chance.getValue();
        if (chanceVal < 100.0) {
            if (ThreadLocalRandom.current().nextDouble(100.0) > chanceVal) {
                return;
            }
        }

        this.engaged = true;
        this.engagedTime.setCooldown((long) duration.getInputMax());
        this.engagedTime.start();
        press();
    }

    private void release() {
        final int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, false);
        Utils.Client.setMouseButtonState(1, false);
    }

    private void press() {
        final int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, true);
        KeyBinding.onTick(key);
        Utils.Client.setMouseButtonState(1, true);
    }
}