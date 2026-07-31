package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "AutoJump", category = Category.Player)
public class AutoJump extends Mod {
    private final BooleanValue b = new BooleanValue("Cancel when shifting", this, true);
    private boolean c = false;
    
    @Override
    public void onDisable() {
        this.ju(this.c = false);
    }
    
    @SubscribeEvent
    public void p(final TickEvent.PlayerTickEvent e) {
        if (Utils.Player.isPlayerInGame()) {
            if (mc.thePlayer.onGround && (!b.isToggled() || !mc.thePlayer.isSneaking())) {
                if (mc.theWorld.getCollidingBoundingBoxes((Entity)mc.thePlayer, mc.thePlayer.getEntityBoundingBox().offset(mc.thePlayer.motionX / 3.0, -1.0, mc.thePlayer.motionZ / 3.0)).isEmpty()) {
                    this.ju(this.c = true);
                }
                else if (this.c) {
                    this.ju(this.c = false);
                }
            }
            else if (this.c) {
                this.ju(this.c = false);
            }
        }
    }
    
    private void ju(final boolean ju) {
        KeyBinding.setKeyBindState(mc.gameSettings.keyBindJump.getKeyCode(), ju);
    }
}
