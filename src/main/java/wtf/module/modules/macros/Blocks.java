package wtf.module.modules.macros;

import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.NumberValue;

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