package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "BridgeAssist", category = Category.Player)
public class BridgeAssist extends Mod {
	private final DescriptionValue desc = new DescriptionValue("Best with fastplace, not autoplace", this);
	private final NumberValue waitFor = new NumberValue("Wait time (ms)", this, 500.0, 0.0, 5000.0, 25.0);
	private final BooleanValue setLook = new BooleanValue("Set look pos", this, true);
    private final BooleanValue onSneak = new BooleanValue("Work only when sneaking", this, true);
    private final BooleanValue workWithSafeWalk = new BooleanValue("Work with safewalk", this, false);
    private final NumberValue assistRange = new NumberValue("Assist range", this, 10.0, 1.0, 40.0, 1.0);
    private final NumberValue glideTime = new NumberValue("Glide speed", this, 500.0, 1.0, 201.0, 5.0);
    private final NumberValue assistMode = new NumberValue("Value", this, 1.0, 1.0, 4.0, 1.0);
    private final DescriptionValue assistModeDesc = new DescriptionValue("Mode: GodBridge", this);
    private boolean waitingForAim;
    private boolean gliding;
    private long startWaitTime;
    private final float[] godbridgePos = new float[] { 75.6f, -315.0f, -225.0f, -135.0f, -45.0f, 0.0f, 45.0f, 135.0f, 225.0f, 315.0f };
    private final float[] moonwalkPos = new float[] { 79.6f, -340.0f, -290.0f, -250.0f, -200.0f, -160.0f, -110.0f, -70.0f, -20.0f, 0.0f, 20.0f, 70.0f, 110.0f, 160.0f, 200.0f, 250.0f, 290.0f, 340.0f };
    private final float[] breezilyPos = new float[] { 79.9f, -360.0f, -270.0f, -180.0f, -90.0f, 0.0f, 90.0f, 180.0f, 270.0f, 360.0f };
    private final float[] normalPos = new float[] { 78.0f, -315.0f, -225.0f, -135.0f, -45.0f, 0.0f, 45.0f, 135.0f, 225.0f, 315.0f };
    private double speedYaw;
    private double speedPitch;
    private float waitingForYaw;
    private float waitingForPitch;
    
    @Override
    public void guiUpdate() {
        this.assistModeDesc.setDesc("Mode: " + Utils.Modes.BridgeMode.values()[(int)(this.assistMode.getInput() - 1.0)].name());
    }
    
    @Override
    public void onEnable() {
        this.waitingForAim = false;
        this.gliding = false;
        super.onEnable();
    }
    
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        
        if (!Utils.Player.playerOverAir() || !mc.thePlayer.onGround) {
            return;
        }
        if (this.onSneak.isToggled() && !mc.thePlayer.isSneaking()) {
            return;
        }
        if (this.gliding) {
            final float fuckedYaw = mc.thePlayer.rotationYaw;
            final float fuckedPitch = mc.thePlayer.rotationPitch;
            final float yaw = fuckedYaw - (int)fuckedYaw / 360 * 360;
            final float pitch = fuckedPitch - (int)fuckedPitch / 360 * 360;
            double ilovebloat1 = yaw - this.speedYaw;
            double ilovebloat2 = yaw + this.speedYaw;
            double ilovebloat3 = pitch - this.speedPitch;
            double ilovebloat4 = pitch + this.speedPitch;
            if (ilovebloat1 < 0.0) {
                ilovebloat1 *= -1.0;
            }
            if (ilovebloat2 < 0.0) {
                ilovebloat2 *= -1.0;
            }
            if (ilovebloat3 < 0.0) {
                ilovebloat3 *= -1.0;
            }
            if (ilovebloat4 < 0.0) {
                ilovebloat4 *= -1.0;
            }
            if (this.speedYaw > ilovebloat1 || this.speedYaw > ilovebloat2) {
                mc.thePlayer.rotationYaw = this.waitingForYaw;
            }
            if (this.speedPitch > ilovebloat3 || this.speedPitch > ilovebloat4) {
                mc.thePlayer.rotationPitch = this.waitingForPitch;
            }
            if (mc.thePlayer.rotationYaw < this.waitingForYaw) {
                final EntityPlayerSP thePlayer = mc.thePlayer;
                thePlayer.rotationYaw += (float)this.speedYaw;
            }
            if (mc.thePlayer.rotationYaw > this.waitingForYaw) {
                final EntityPlayerSP thePlayer2 = mc.thePlayer;
                thePlayer2.rotationYaw -= (float)this.speedYaw;
            }
            if (mc.thePlayer.rotationPitch > this.waitingForPitch) {
                final EntityPlayerSP thePlayer3 = mc.thePlayer;
                thePlayer3.rotationPitch -= (float)this.speedPitch;
            }
            if (mc.thePlayer.rotationYaw == this.waitingForYaw && mc.thePlayer.rotationPitch == this.waitingForPitch) {
                this.gliding = false;
                this.waitingForAim = false;
            }
            return;
        }
        if (!this.waitingForAim) {
            this.waitingForAim = true;
            this.startWaitTime = System.currentTimeMillis();
            return;
        }
        if (System.currentTimeMillis() - this.startWaitTime < this.waitFor.getInput()) {
            return;
        }
        final float fuckedYaw = mc.thePlayer.rotationYaw;
        final float fuckedPitch = mc.thePlayer.rotationPitch;
        final float yaw = fuckedYaw - (int)fuckedYaw / 360 * 360;
        final float pitch = fuckedPitch - (int)fuckedPitch / 360 * 360;
        final float range = (float)this.assistRange.getInput();
        switch (Utils.Modes.BridgeMode.values()[(int)(this.assistMode.getInput() - 1.0)]) {
            case GODBRIDGE: {
                if (this.godbridgePos[0] >= pitch - range && this.godbridgePos[0] <= pitch + range) {
                    for (int k = 1; k < this.godbridgePos.length; ++k) {
                        if (this.godbridgePos[k] >= yaw - range && this.godbridgePos[k] <= yaw + range) {
                            this.aimAt(this.godbridgePos[0], this.godbridgePos[k], fuckedYaw, fuckedPitch);
                            this.waitingForAim = false;
                            return;
                        }
                    }
                }
            }
            case MOONWALK: {
                if (this.moonwalkPos[0] >= pitch - range && this.moonwalkPos[0] <= pitch + range) {
                    for (int k = 1; k < this.moonwalkPos.length; ++k) {
                        if (this.moonwalkPos[k] >= yaw - range && this.moonwalkPos[k] <= yaw + range) {
                            this.aimAt(this.moonwalkPos[0], this.moonwalkPos[k], fuckedYaw, fuckedPitch);
                            this.waitingForAim = false;
                            return;
                        }
                    }
                }
            }
            case BREEZILY: {
                if (this.breezilyPos[0] >= pitch - range && this.breezilyPos[0] <= pitch + range) {
                    for (int k = 1; k < this.breezilyPos.length; ++k) {
                        if (this.breezilyPos[k] >= yaw - range && this.breezilyPos[k] <= yaw + range) {
                            this.aimAt(this.breezilyPos[0], this.breezilyPos[k], fuckedYaw, fuckedPitch);
                            this.waitingForAim = false;
                            return;
                        }
                    }
                }
            }
            case NORMAL: {
                if (this.normalPos[0] >= pitch - range && this.normalPos[0] <= pitch + range) {
                    for (int k = 1; k < this.normalPos.length; ++k) {
                        if (this.normalPos[k] >= yaw - range && this.normalPos[k] <= yaw + range) {
                            this.aimAt(this.normalPos[0], this.normalPos[k], fuckedYaw, fuckedPitch);
                            this.waitingForAim = false;
                            return;
                        }
                    }
                    break;
                }
                break;
            }
        }
        this.waitingForAim = false;
    }
    
    public void aimAt(final float pitch, final float yaw, final float fuckedYaw, final float fuckedPitch) {
        if (this.setLook.isToggled()) {
            mc.thePlayer.rotationPitch = pitch + (int)fuckedPitch / 360 * 360;
            mc.thePlayer.rotationYaw = yaw;
        }
    }
}
