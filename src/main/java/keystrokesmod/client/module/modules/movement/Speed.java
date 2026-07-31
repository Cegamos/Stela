package keystrokesmod.client.module.modules.movement;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;

@ModuleInfo(name = "Speed", category = Category.Movement)
public class Speed extends Mod {
    private final DescriptionSetting dc = new DescriptionSetting("Hypixel max: 1.13", this);
    private final SliderSetting a = new SliderSetting("Speed", this, 1.2, 1.0, 1.5, 0.01);
    private final TickSetting b = new TickSetting("Strafe only", this, false);

    @Override
    public void update() {
        final double csp = Utils.Player.pythagorasMovement();
        if (csp != 0.0 && mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying && (!b.isToggled() || mc.thePlayer.moveStrafing != 0.0f) && (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            final double val = a.getInput() - (a.getInput() - 1.0) * 0.5;
            Utils.Player.fixMovementSpeed(csp * val, true);
        }
    }
}
