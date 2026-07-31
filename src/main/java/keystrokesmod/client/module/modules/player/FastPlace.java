package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "FastPlace", category = Category.Player)
public class FastPlace extends Mod {
    private final NumberValue delaySlider = new NumberValue("Delay", this, 0.0, 0.0, 4.0, 1.0);
    private final BooleanValue blockOnly = new BooleanValue("Blocks only", this, true);

    @SubscribeEvent
    public void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.END && Utils.Player.isPlayerInGame() && mc.inGameHasFocus) {
            if (blockOnly.isToggled()) {
                final ItemStack item = mc.thePlayer.getHeldItem();
                if (item == null || !(item.getItem() instanceof ItemBlock)) {
                    return;
                }
            }

            ReflectUtil.setRightClickDelayTimer((int) delaySlider.getInput());
        }
    }
}