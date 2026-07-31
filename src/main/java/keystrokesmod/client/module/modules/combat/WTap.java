package keystrokesmod.client.module.modules.combat;

import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PreRenderTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.Utils.Modes.SprintResetTimings;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

@ModuleInfo(name = "WTap", category = Category.Combat)
public class WTap extends Mod {
    public BooleanValue onlyPlayers = new BooleanValue("Only combo players", this, true);
    public RangeValue actionTicks = new RangeValue("Action Time (MS)", this, 25.0, 55.0, 1.0, 500.0, 1.0);
    public RangeValue onceEvery = new RangeValue("Once every ... hits", this, 1.0, 1.0, 1.0, 10.0, 1.0);
    private final RangeValue postDelay = new RangeValue("Post delay (MS)", this, 25.0, 55.0, 1.0, 500.0, 1.0);
    private final NumberValue chance = new NumberValue("Chance %", this, 100.0, 0.0, 100.0, 1.0);
    private final NumberValue range = new NumberValue("Range: ", this, 3.0, 1.0, 6.0, 0.05);
    private final ModeValue mode = new ModeValue("Mode", this, SprintResetTimings.PRE, SprintResetTimings.values());
    public boolean comboing;
    public boolean hitCoolDown;
    public boolean alreadyHit;
    public boolean waitingForPostDelay;
    public int hitTimeout;
    public int hitsWaited;
    public Clock actionTimer = new Clock(0L);
    public Clock postDelayTimer = new Clock(0L);
    
    @EventLink
    private Listener<PreRenderTickEvent> preRenderTick = event -> both();
    
    @EventLink
    private Listener<PostRenderTickEvent> postRenderTick = event -> both();
    
    private void both() {
    	if (!Utils.Player.isPlayerInGame()) {
            return;
        }
    	
        if (waitingForPostDelay) {
            if (postDelayTimer.hasFinished()) {
                waitingForPostDelay = false;
                comboing = true;
                startCombo();
                actionTimer.start();
            }
            return;
        }
        if (!comboing) {
            if (mc.objectMouseOver != null && mc.objectMouseOver.entityHit instanceof Entity && Mouse.isButtonDown(0)) {
                final Entity target = mc.objectMouseOver.entityHit;
                if (target.isDead) {
                    return;
                }
                if (mc.thePlayer.getDistanceToEntity(target) <= range.getValue()) {
                    if ((target.hurtResistantTime >= 10 && mode.is(SprintResetTimings.POST)) || (target.hurtResistantTime <= 10 && mode.is(SprintResetTimings.PRE))) {
                        if (onlyPlayers.getValue() && !(target instanceof EntityPlayer)) {
                            return;
                        }
    
                        if (hitCoolDown && !alreadyHit) {
                            ++hitsWaited;
                            if (hitsWaited < hitTimeout) {
                                alreadyHit = true;
                                return;
                            }
                            hitCoolDown = false;
                            hitsWaited = 0;
                        }
                        if (chance.getValue() != 100.0 && Math.random() > chance.getValue() / 100.0) {
                            return;
                        }
                        if (!alreadyHit) {
                            this.guiUpdate();
                            if (onceEvery.getInputMin() == onceEvery.getInputMax()) {
                                hitTimeout = (int)onceEvery.getInputMin();
                            }
                            else {
                                hitTimeout = ThreadLocalRandom.current().nextInt((int)onceEvery.getInputMin(), (int)onceEvery.getInputMax());
                            }
                            hitCoolDown = true;
                            hitsWaited = 0;
                            actionTimer.setCooldown((long)ThreadLocalRandom.current().nextDouble(actionTicks.getInputMin(), actionTicks.getInputMax() + 0.01));
                            if (postDelay.getInputMax() != 0.0) {
                                postDelayTimer.setCooldown((long)ThreadLocalRandom.current().nextDouble(postDelay.getInputMin(), postDelay.getInputMax() + 0.01));
                                postDelayTimer.start();
                                waitingForPostDelay = true;
                            }
                            else {
                                comboing = true;
                                startCombo();
                                actionTimer.start();
                                alreadyHit = true;
                            }
                            alreadyHit = true;
                        }
                    }
                    else {
                        if (alreadyHit) {}
                        alreadyHit = false;
                    }
                }
            }
            return;
        }
        if (actionTimer.hasFinished()) {
            comboing = false;
            finishCombo();
        }
    }
    
    private void finishCombo() {
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), true);
        }
    }
    
    private void startCombo() {
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindForward.getKeyCode(), false);
            KeyBinding.onTick(mc.gameSettings.keyBindForward.getKeyCode());
        }
    }
}
