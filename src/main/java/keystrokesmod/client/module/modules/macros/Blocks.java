package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Blocks", category = Category.Macros)
public class Blocks extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Prefer which slot", this, 9.0, 1.0, 9.0, 1.0);

    @Override
    public void onEnable() {
        super.onEnable();
        
        if (checkGame()) {
            this.disable();
            return;
        }

        if (this.preferSlot.getValue()) {
            final int preferredSlot = (int) this.hotbarSlotPreference.getValue() - 1;
            final ItemStack itemInSlot = getPlayer().inventory.getStackInSlot(preferredSlot);
            
            if (itemInSlot != null && itemInSlot.getItem() instanceof ItemBlock) {
                getPlayer().inventory.currentItem = preferredSlot;
                this.disable();
                return;
            }
        }

        for (int slot = 0; slot <= 8; ++slot) {
            final ItemStack itemInSlot = getPlayer().inventory.getStackInSlot(slot);
            
            if (itemInSlot != null && itemInSlot.getItem() instanceof ItemBlock) {
                final ItemBlock itemBlock = (ItemBlock) itemInSlot.getItem();
                final Block block = itemBlock.getBlock();
                
                if (block.isFullBlock() || block.isFullCube()) {
                    if (getPlayer().inventory.currentItem != slot) {
                        getPlayer().inventory.currentItem = slot;
                    }
                    this.disable();
                    return;
                }
            }
        }

        this.disable();
    }
}