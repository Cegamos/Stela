package wtf.module.modules.macros;

import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;

@ModuleInfo(name = "Pearl", category = Category.Macros)
public class Pearl extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 6.0, 1.0, 9.0, 1.0);

    @Override
    public void onEnable() {
    	super.onEnable();
        if (checkGame()) {
            this.disable();
            return;
        }

        int slot = -1;

        if (preferSlot.getValue()) {
            int preferredSlot = (int) hotbarSlotPreference.getValue() - 1;
            if (isEnderPearl(preferredSlot)) {
                slot = preferredSlot;
            }
        } 
        
        if (slot == -1) {
            for (int i = 0; i <= 8; i++) {
                if (isEnderPearl(i)) {
                    slot = i;
                    break;
                }
            }
        }

        if (slot != -1 && getPlayer().inventory.currentItem != slot) {
            getPlayer().inventory.currentItem = slot;
        }

        this.disable();
    }

    private boolean isEnderPearl(int slot) {
        ItemStack stack = getPlayer().inventory.getStackInSlot(slot);
        if (stack != null && stack.getItem() != null) {
            return stack.getItem() == Items.ender_pearl;
        }
        return false;
    }
}