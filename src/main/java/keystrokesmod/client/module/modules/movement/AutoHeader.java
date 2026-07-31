package keystrokesmod.client.module.modules.movement;

import org.lwjgl.input.Keyboard;

import io.netty.util.internal.ThreadLocalRandom;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "AutoHeader", category = Category.Movement)
public class AutoHeader extends Mod {
    private final DescriptionSetting desc = new DescriptionSetting("Spams spacebar when under blocks", this);
    private final TickSetting cancelDuringShift = new TickSetting("Cancel if snkeaing", this, true);
    private final TickSetting onlyWhenHoldingSpacebar = new TickSetting("Only when holding jump", this, true);
    private final SliderSetting pbs = new SliderSetting("Jump Presses per second", this, 12.0, 1.0, 20.0, 1.0);
    private double startWait;
    
    @Override
    public void onEnable() {
        this.startWait = (double)System.currentTimeMillis();
        super.onEnable();
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.RenderTickEvent e) {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
            return;
        }
        if (cancelDuringShift.isToggled() && mc.thePlayer.isSneaking()) {
            return;
        }
        if (onlyWhenHoldingSpacebar.isToggled() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            return;
        }
        if (Utils.Player.playerUnderBlock() && mc.thePlayer.onGround && this.startWait + 1000.0 / ThreadLocalRandom.current().nextDouble(pbs.getInput() - 0.543543, pbs.getInput() + 1.32748923) < System.currentTimeMillis()) {
            mc.thePlayer.jump();
            this.startWait = (double)System.currentTimeMillis();
        }
    }
}
