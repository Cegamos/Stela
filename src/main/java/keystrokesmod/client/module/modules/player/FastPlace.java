package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostPlayerTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "FastPlace", category = Category.Player)
public class FastPlace extends Mod {
    private final NumberValue delaySlider = new NumberValue("Delay", this, 0.0, 0.0, 4.0, 1.0);
    private final BooleanValue blockOnly = new BooleanValue("Blocks only", this, true);
    
    @EventLink
    private Listener<PostPlayerTickEvent> playerTick = event -> {
        if (Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
            if (blockOnly.isToggled()) {
                final ItemStack item = mc.thePlayer.getHeldItem();
                if (item == null || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            ReflectUtil.setRightClickDelayTimer((int) delaySlider.getInput());
        }
    };
}