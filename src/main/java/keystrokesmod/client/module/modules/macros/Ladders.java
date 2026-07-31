package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Ladders", category = Category.Macros)
public class Ladders extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 8.0, 1.0, 9.0, 1.0);

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        int slot = -1;

        if (preferSlot.getValue()) {
            int preferredSlot = (int) hotbarSlotPreference.getValue() - 1;
            if (isLadder(preferredSlot)) {
                slot = preferredSlot;
            }
        } else {
            for (int i = 0; i <= 8; i++) {
                if (isLadder(i)) {
                    slot = i;
                    break;
                }
            }
        }

        if (slot != -1 && mc.thePlayer.inventory.currentItem != slot) {
            mc.thePlayer.inventory.currentItem = slot;
        }

        this.onDisable();
        this.disable();
    }

    private boolean isLadder(int slot) {
        ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
        return stack != null && "ladder".equalsIgnoreCase(stack.getDisplayName());
    }
}