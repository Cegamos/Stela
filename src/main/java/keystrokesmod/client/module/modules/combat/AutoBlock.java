package keystrokesmod.client.module.modules.combat;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.utils.timing.Clock;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "AutoBlock", category = Category.Combat)
public class AutoBlock extends Mod {
    public DoubleSliderSetting duration = new DoubleSliderSetting("Block duration (MS)", this, 20.0, 100.0, 1.0, 500.0, 1.0);
    public DoubleSliderSetting distance = new DoubleSliderSetting("Distance to player (blocks)", this, 0.0, 3.0, 0.0, 6.0, 0.01);
    public SliderSetting chance = new SliderSetting("Chance %", this, 100.0, 0.0, 100.0, 1.0);
    private boolean engaged;
    private Clock engagedTime = new Clock(0);
    
    @SubscribeEvent
    public void yes(final TickEvent.RenderTickEvent e) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        if (this.engaged) {
            if ((this.engagedTime.hasFinished() || !Mouse.isButtonDown(0)) && duration.getInputMin() <= this.engagedTime.getElapsedTime()) {
                this.engaged = false;
                release();
            }
            return;
        }
        if (Mouse.isButtonDown(0) && mc.objectMouseOver != null && mc.objectMouseOver.entityHit != null && mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit) >= distance.getInputMin() && mc.objectMouseOver.entityHit != null && mc.thePlayer.getDistanceToEntity(mc.objectMouseOver.entityHit) <= distance.getInputMax() && (chance.getInput() == 100.0 || Math.random() <= chance.getInput() / 100.0)) {
            this.engaged = true;
            this.engagedTime.setCooldown((long)duration.getInputMax());
            this.engagedTime.start();
            press();
        }
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
