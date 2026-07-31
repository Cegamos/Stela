package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.item.Item;
import net.minecraft.item.ItemAppleGold;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemSoup;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Healing", category = Category.Macros)
public class Healing extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 8.0, 1.0, 9.0, 1.0);
    private final NumberValue itemMode = new NumberValue("Healing item", this, 1.0, 1.0, HealingItems.values().length, 1.0);
    private final DescriptionValue modeDesc = new DescriptionValue("Mode: SOUP", this);

    @Override
    public void guiUpdate() {
        modeDesc.setDesc("Mode: " + getCurrentHealingItem().name());
    }

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        HealingItems mode = getCurrentHealingItem();

        if (preferSlot.getValue()) {
            int preferredSlot = (int) hotbarSlotPreference.getValue() - 1;
            if (isValidHealingItem(preferredSlot, mode)) {
                Healing.mc.thePlayer.inventory.currentItem = preferredSlot;
                this.disable();
                return;
            }
        }

        for (int slot = 0; slot <= 8; ++slot) {
            if (isValidHealingItem(slot, mode)) {
                Healing.mc.thePlayer.inventory.currentItem = slot;
                break;
            }
        }

        this.onDisable();
        this.disable();
    }

    private HealingItems getCurrentHealingItem() {
        return HealingItems.values()[(int) itemMode.getValue() - 1];
    }

    private boolean isValidHealingItem(int slot, HealingItems type) {
        ItemStack stack = Healing.mc.thePlayer.inventory.getStackInSlot(slot);
        if (stack == null) return false;
        Item item = stack.getItem();

        switch (type) {
            case SOUP:
                return item instanceof ItemSoup;
            case GAPPLE:
                return item instanceof ItemAppleGold;
            case FOOD:
                return item instanceof ItemFood;
            case ALL:
                return item instanceof ItemSoup || item instanceof ItemAppleGold || item instanceof ItemFood;
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
