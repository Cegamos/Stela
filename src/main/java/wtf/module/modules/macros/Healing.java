package wtf.module.modules.macros;

import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.DescriptionValue;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;

@ModuleInfo(name = "Healing", category = Category.Macros)
public class Healing extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 8.0, 1.0, 9.0, 1.0);
    
    private static final HealingItems[] cachedHealingItems = HealingItems.values();
    
    private final NumberValue itemMode = new NumberValue("Healing item", this, 1.0, 1.0, cachedHealingItems.length, 1.0);
    private final DescriptionValue modeDesc = new DescriptionValue("Mode: SOUP", this);

    @Override
    public void guiUpdate() {
        modeDesc.setDesc("Mode: " + getCurrentHealingItem().name());
    }

    @Override
    public void onEnable() {
        super.onEnable();
        
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        final HealingItems mode = getCurrentHealingItem();

        if (preferSlot.getValue()) {
            final int preferredSlot = (int) hotbarSlotPreference.getValue() - 1;
            if (isValidHealingItem(preferredSlot, mode)) {
                getPlayer().inventory.currentItem = preferredSlot;
                this.disable();
                return;
            }
        }

        for (int slot = 0; slot <= 8; ++slot) {
            if (isValidHealingItem(slot, mode)) {
                getPlayer().inventory.currentItem = slot;
                break;
            }
        }

        this.disable();
    }

    private HealingItems getCurrentHealingItem() {
        return cachedHealingItems[(int) itemMode.getValue() - 1];
    }

    private boolean isValidHealingItem(int slot, HealingItems type) {
        final ItemStack stack = getPlayer().inventory.getStackInSlot(slot);
        if (stack == null) return false;
        
        final Item item = stack.getItem();

        switch (type) {
            case SOUP:
                return item instanceof ItemSoup;
            case GAPPLE:
                return item instanceof ItemAppleGold;
            case FOOD:
            case ALL:
                return item instanceof ItemFood;
            default:
                return false;
        }
    }

    public enum HealingItems {
        SOUP,
        GAPPLE,
        FOOD,
        ALL
    }
}