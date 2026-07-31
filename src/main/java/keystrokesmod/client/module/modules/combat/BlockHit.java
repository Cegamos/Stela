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
import net.minecraft.util.MovingObjectPosition;

@ModuleInfo(name = "BlockHit", category = Category.Combat)
public class BlockHit extends Mod {
    public final BooleanValue onlyPlayers = new BooleanValue("Only combo players", this, true);
    public final BooleanValue onRightMBHold = new BooleanValue("When holding down rmb", this, true);
    public final RangeValue waitMs = new RangeValue("Action Time (MS)", this, 110.0, 150.0, 1.0, 500.0, 1.0);
    public final RangeValue hitPer = new RangeValue("Once every ... hits", this, 1.0, 1.0, 1.0, 10.0, 1.0);
    public final RangeValue postDelay = new RangeValue("Post Delay (MS)", this, 10.0, 40.0, 0.0, 500.0, 1.0);
    public final NumberValue chance = new NumberValue("Chance %", this, 100.0, 0.0, 100.0, 1.0);
    public final NumberValue range = new NumberValue("Range: ", this, 3.0, 1.0, 6.0, 0.05);
    
    private final ModeValue mode = new ModeValue("Mode", this, SprintResetTimings.PRE, SprintResetTimings.values());

    public boolean executingAction;
    public boolean hitCoolDown;
    public boolean alreadyHit;
    public boolean safeGuard;
    public int hitTimeout;
    public int hitsWaited;
    
    private final Clock actionTimer = new Clock(0);
    private final Clock postDelayTimer = new Clock(0);
    private boolean waitingForPostDelay;
    
    @EventLink
    private final Listener<PreRenderTickEvent> preRenderTick = event -> both();
    
    @EventLink
    private final Listener<PostRenderTickEvent> postRenderTick = event -> both();
    
    private void both() {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }

        final boolean isRightMBHold = onRightMBHold.getValue();
        final boolean tryingToCombo = Utils.Player.tryingToCombo();

        if (isRightMBHold && !tryingToCombo) {
            if (!safeGuard || (Utils.Player.isPlayerHoldingWeapon() && Mouse.isButtonDown(0))) {
                safeGuard = true;
                finishCombo();
            }
            return;
        }
        
        if (this.waitingForPostDelay) {
            if (this.postDelayTimer.hasFinished()) {
                executingAction = true;
                startCombo();
                this.waitingForPostDelay = false;
                if (safeGuard) safeGuard = false;
                this.actionTimer.start();
            }
            return;
        }
        
        if (!executingAction) {
            final MovingObjectPosition mop = mc.objectMouseOver;
            final boolean hasEntityHit = mop != null && mop.entityHit != null;

            if (isRightMBHold && tryingToCombo) {
                if (!hasEntityHit || mop.entityHit.isDead) {
                    if (!safeGuard || (Utils.Player.isPlayerHoldingWeapon() && Mouse.isButtonDown(0))) {
                        safeGuard = true;
                        finishCombo();
                    }
                    return;
                }
            }
            
            if (hasEntityHit && mop.entityHit instanceof Entity && Mouse.isButtonDown(0)) {
                final Entity target = mop.entityHit;
                
                if (target.isDead) {
                    if (isRightMBHold && Mouse.isButtonDown(1) && (!safeGuard || Utils.Player.isPlayerHoldingWeapon())) {
                        safeGuard = true;
                        finishCombo();
                    }
                    return;
                }
                
                if (mc.thePlayer.getDistanceToEntity(target) <= range.getValue()) {
                    
                    final boolean isValidTiming = 
                        (target.hurtResistantTime >= 10 && mode.is(SprintResetTimings.POST)) || 
                        (target.hurtResistantTime <= 10 && mode.is(SprintResetTimings.PRE));

                    if (isValidTiming) {
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
                        
                        final double currentChance = chance.getValue();
                        if (currentChance != 100.0 && ThreadLocalRandom.current().nextDouble() > currentChance / 100.0) {
                            return;
                        }
                        
                        if (!alreadyHit) {
                            this.guiUpdate();
                            
                            final double minHit = hitPer.getInputMin();
                            final double maxHit = hitPer.getInputMax();
                            hitTimeout = (minHit == maxHit) ? (int)minHit : ThreadLocalRandom.current().nextInt((int)minHit, (int)maxHit);
                            
                            hitCoolDown = true;
                            hitsWaited = 0;
                            
                            this.actionTimer.setCooldown((long)ThreadLocalRandom.current().nextDouble(waitMs.getInputMin(), waitMs.getInputMax() + 0.01));
                            
                            final double maxPost = postDelay.getInputMax();
                            if (maxPost != 0.0) {
                                this.postDelayTimer.setCooldown((long)ThreadLocalRandom.current().nextDouble(postDelay.getInputMin(), maxPost + 0.01));
                                this.postDelayTimer.start();
                                this.waitingForPostDelay = true;
                            } else {
                                executingAction = true;
                                startCombo();
                                this.actionTimer.start();
                                alreadyHit = true;
                                if (safeGuard) safeGuard = false;
                            }
                            alreadyHit = true;
                        }
                    } else {
                        if (alreadyHit) alreadyHit = false;
                        if (safeGuard) safeGuard = false;
                    }
                }
            }
            return;
        }
        
        if (this.actionTimer.hasFinished()) {
            executingAction = false;
            finishCombo();
        }
    }

    private void finishCombo() {
        final int key = mc.gameSettings.keyBindUseItem.getKeyCode();
        KeyBinding.setKeyBindState(key, false);
        Utils.Client.setMouseButtonState(1, false);
    }
    
    private void startCombo() {
        if (Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode())) {
            final int key = mc.gameSettings.keyBindUseItem.getKeyCode();
            KeyBinding.setKeyBindState(key, true);
            KeyBinding.onTick(key);
            Utils.Client.setMouseButtonState(1, true);
        }
    }
}