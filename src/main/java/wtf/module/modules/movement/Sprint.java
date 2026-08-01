package wtf.module.modules.movement;

import net.minecraft.client.settings.KeyBinding;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PreTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.util.Utils;

@ModuleInfo(name = "Sprint", category = Category.Movement)
public class Sprint extends Mod {
    private final BooleanValue o = new BooleanValue("OmniSprint", this, false);
    public final BooleanValue ignoreBlindness = new BooleanValue("Ignore Blindness", this, true);
    public final BooleanValue multiDir = new BooleanValue("Multi Direction", this, false);

    @EventLink
    public final Listener<PreTickEvent> onTick = e -> {
        if (!Utils.Player.isPlayerInGame() || !mc.inGameHasFocus) return;

        if (o.getValue()) {
            if (Utils.Player.isMoving() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    };
}
