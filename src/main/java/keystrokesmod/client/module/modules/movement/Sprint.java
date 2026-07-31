package keystrokesmod.client.module.modules.movement;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.settings.KeyBinding;

@ModuleInfo(name = "Sprint", category = Category.Movement)
public class Sprint extends Mod {
    private final TickSetting o = new TickSetting("OmniSprint", this, false);
    public final TickSetting ignoreBlindness = new TickSetting("Ignore Blindness", this, true);
    public final TickSetting multiDir = new TickSetting("Multi Direction", this, false);

    @EventLink
    public final Listener<PreTickEvent> onTick = e -> {
        if (!Utils.Player.isPlayerInGame() || !mc.inGameHasFocus) return;

        if (o.isToggled()) {
            if (Utils.Player.isMoving() && mc.thePlayer.getFoodStats().getFoodLevel() > 6) {
                mc.thePlayer.setSprinting(true);
            }
        } else {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindSprint.getKeyCode(), true);
        }
    };
}
