package wtf.module.modules.macros;

import net.minecraft.item.ItemEgg;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemSnowball;
import net.minecraft.item.ItemStack;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;

@ModuleInfo(name = "Trajectories", category = Category.Macros)
public class Trajectories extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 5.0, 1.0, 9.0, 1.0);

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        int targetSlot = -1;

        if (preferSlot.getValue()) {
            int preferredSlot = (int) hotbarSlotPreference.getValue() - 1;
            if (isThrowable(preferredSlot)) {
                targetSlot = preferredSlot;
            }
        } 
        
        if (targetSlot == -1) {
            for (int i = 0; i <= 8; i++) {
                if (isThrowable(i)) {
                    targetSlot = i;
                    break;
                }
            }
        }

        if (targetSlot != -1 && getPlayer().inventory.currentItem != targetSlot) {
        	getPlayer().inventory.currentItem = targetSlot;
        }

        this.disable();
    }

    private boolean isThrowable(int slot) {
        ItemStack item = getPlayer().inventory.getStackInSlot(slot);
        
        if (item != null && item.getItem() != null) {
            return item.getItem() instanceof ItemSnowball || 
                   item.getItem() instanceof ItemEgg || 
                   item.getItem() instanceof ItemFishingRod;
        }
        
        return false;
    }
}