package keystrokesmod.client.module.modules.movement;

import org.lwjgl.input.Keyboard;

import io.netty.util.internal.ThreadLocalRandom;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PreRenderTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;

@ModuleInfo(name = "AutoHeader", category = Category.Movement)
public class AutoHeader extends Mod {
    private final DescriptionValue desc = new DescriptionValue("Spams spacebar when under blocks", this);
    private final BooleanValue cancelDuringShift = new BooleanValue("Cancel if snkeaing", this, true);
    private final BooleanValue onlyWhenHoldingSpacebar = new BooleanValue("Only when holding jump", this, true);
    private final NumberValue pbs = new NumberValue("Jump Presses per second", this, 12.0, 1.0, 20.0, 1.0);
    private double startWait;
    
    @Override
    public void onEnable() {
    	super.onEnable();
        this.startWait = (double)System.currentTimeMillis();
    }
    
    @EventLink
    private final Listener<PreRenderTickEvent> preRenderTick = event -> both();

    @EventLink
    private final Listener<PostRenderTickEvent> postRenderTick = event -> both();

    private void both() {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
            return;
        }
        if (cancelDuringShift.getValue() && mc.thePlayer.isSneaking()) {
            return;
        }
        if (onlyWhenHoldingSpacebar.getValue() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            return;
        }
        if (Utils.Player.playerUnderBlock() && mc.thePlayer.onGround && this.startWait + 1000.0 / ThreadLocalRandom.current().nextDouble(pbs.getValue() - 0.543543, pbs.getValue() + 1.32748923) < System.currentTimeMillis()) {
            mc.thePlayer.jump();
            this.startWait = (double)System.currentTimeMillis();
        }
    }
}
