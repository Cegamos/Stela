package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;

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