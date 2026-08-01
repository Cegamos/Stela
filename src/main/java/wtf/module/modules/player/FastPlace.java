package wtf.module.modules.player;

import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PostPlayerTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;
import wtf.util.system.ReflectUtil;

@ModuleInfo(name = "FastPlace", category = Category.Player)
public class FastPlace extends Mod {
    private final NumberValue delaySlider = new NumberValue("Delay", this, 0.0, 0.0, 4.0, 1.0);
    private final BooleanValue blockOnly = new BooleanValue("Blocks only", this, true);
    
    @EventLink
    private Listener<PostPlayerTickEvent> playerTick = event -> {
        if (Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
            if (blockOnly.getValue()) {
                final ItemStack item = mc.thePlayer.getHeldItem();
                if (item == null || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            ReflectUtil.setRightClickDelayTimer((int) delaySlider.getValue());
        }
    };
}