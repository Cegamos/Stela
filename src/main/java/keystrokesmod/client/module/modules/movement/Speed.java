package keystrokesmod.client.module.modules.movement;

import org.lwjgl.input.Keyboard;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;

@ModuleInfo(name = "Speed", category = Category.Movement)
public class Speed extends Mod {
    private final DescriptionValue dc = new DescriptionValue("Hypixel max: 1.13", this);
    private final NumberValue a = new NumberValue("Speed", this, 1.2, 1.0, 1.5, 0.01);
    private final BooleanValue b = new BooleanValue("Strafe only", this, false);

    @Override
    public void update() {
        final double csp = Utils.Player.pythagorasMovement();
        if (csp != 0.0 && mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying && (!b.getValue() || mc.thePlayer.moveStrafing != 0.0f) && (mc.thePlayer.hurtTime != mc.thePlayer.maxHurtTime || mc.thePlayer.maxHurtTime <= 0) && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            final double val = a.getValue() - (a.getValue() - 1.0) * 0.5;
            Utils.Player.fixMovementSpeed(csp * val, true);
        }
    }
}
