package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Armor", category = Category.Macros)
public class Armour extends Mod {
    public BooleanValue ignoreIfAlreadyEquipped = new BooleanValue("Ignore if already equipped", this, true);

    @Override
    public void onEnable() {
        super.onEnable();
        if (checkGame()) return;

        final boolean ignoreEquipped = ignoreIfAlreadyEquipped.getValue();

        for (int armorType = 0; armorType < 4; ++armorType) {
            final int armorInventorySlot = 3 - armorType;
            final ItemStack equippedStack = getPlayer().getCurrentArmor(armorInventorySlot);
            
            final boolean hasEquipped = equippedStack != null && equippedStack.getItem() instanceof ItemArmor;

            if (ignoreEquipped && hasEquipped) {
                continue;
            }

            double bestDefense = -1.0;
            if (hasEquipped) {
                bestDefense = ((ItemArmor) equippedStack.getItem()).getArmorMaterial().getDamageReductionAmount(armorType);
            }

            int bestSlot = -1;

            for (int slot = 0; slot <= 8; ++slot) {
                final ItemStack hotbarStack = mc.thePlayer.inventory.getStackInSlot(slot);
                
                if (hotbarStack != null && hotbarStack.getItem() instanceof ItemArmor) {
                    final ItemArmor hotbarArmor = (ItemArmor) hotbarStack.getItem();
                    
                    if (hotbarArmor.armorType == armorType) {
                        final double defense = hotbarArmor.getArmorMaterial().getDamageReductionAmount(armorType);
                        
                        if (defense > bestDefense) {
                            bestDefense = defense;
                            bestSlot = slot;
                        }
                    }
                }
            }

            if (bestSlot != -1) {
                getPlayer().inventory.currentItem = bestSlot;
                this.disable();
                return;
            }
        }

        this.disable();
    }
}