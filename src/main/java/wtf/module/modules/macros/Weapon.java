package wtf.module.modules.macros;

import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemTool;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import wtf.module.*;
import wtf.module.modules.Mod;
import wtf.util.*;

@ModuleInfo(name = "Weapon", category = Category.Macros)
public class Weapon extends Mod {

    @Override
    public void onEnable() {
        super.onEnable();
        
        if (checkGame()) {
            this.disable();
            return;
        }

        int bestSlot = -1;
        float highestDamage = Float.NEGATIVE_INFINITY;

        for (int slot = 0; slot < 9; slot++) {
            final ItemStack stack = getPlayer().inventory.getStackInSlot(slot);
            
            if (stack == null) {
                continue;
            }

            final float damage = calculateItemDamage(stack);

            if (damage > highestDamage) {
                highestDamage = damage;
                bestSlot = slot;
            }
        }

        if (bestSlot != -1 && getPlayer().inventory.currentItem != bestSlot) {
            Utils.Player.hotkeyToSlot(bestSlot);
        }

        this.onDisable();
        this.disable();
    }

    private float calculateItemDamage(final ItemStack stack) {
        float damage = 1f;
        final Item item = stack.getItem();

        if (item instanceof ItemSword) {
            damage += ((ItemSword) item).getDamageVsEntity();
        } else if (item instanceof ItemTool) {
            damage += ((ItemTool) item).getToolMaterial().getDamageVsEntity();
        }

        final NBTTagList enchantments = stack.getEnchantmentTagList();
        
        if (enchantments != null) {
            final int tagCount = enchantments.tagCount();
            
            for (int i = 0; i < tagCount; i++) {
                final NBTTagCompound nbt = enchantments.getCompoundTagAt(i);
                
                if (nbt != null && nbt.getShort("id") == 16) {
                    final int level = nbt.getShort("lvl");
                    damage += 1.25f * level; 
                    break;
                }
            }
        }

        return damage;
    }
}