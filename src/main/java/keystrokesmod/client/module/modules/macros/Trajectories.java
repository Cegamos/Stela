package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Trajectories", category = Category.Macros)
public class Trajectories extends Mod {
    private final TickSetting preferSlot = new TickSetting("Prefer a slot", this, false);
    private final SliderSetting hotbarSlotPreference = new SliderSetting("Preferred slot", this, 5.0, 1.0, 9.0, 1.0);

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        int targetSlot = -1;

        if (preferSlot.isToggled()) {
            int preferredSlot = (int) hotbarSlotPreference.getInput() - 1;
            if (isThrowable(preferredSlot)) {
                targetSlot = preferredSlot;
            }
        } else {
            for (int i = 0; i <= 8; i++) {
                if (isThrowable(i)) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (targetSlot != -1 && mc.thePlayer.inventory.currentItem != targetSlot) {
            mc.thePlayer.inventory.currentItem = targetSlot;
        }

        this.onDisable();
        this.disable();
    }

    private boolean isThrowable(int slot) {
        ItemStack item = mc.thePlayer.inventory.getStackInSlot(slot);
        return item != null && (
            item.getItem() instanceof ItemSnowball ||
            item.getItem() instanceof ItemEgg ||
            item.getItem() instanceof ItemFishingRod
        );
    }
}
