package keystrokesmod.client.module.modules.macros;

import java.util.ArrayList;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Pearl", category = Category.Macros)
public class Pearl extends Mod {
    private final BooleanValue preferSlot = new BooleanValue("Prefer a slot", this, false);
    private final NumberValue hotbarSlotPreference = new NumberValue("Preferred slot", this, 6.0, 1.0, 9.0, 1.0);
    public static final ArrayList<KeyBinding> changedKeybinds = new ArrayList<>();

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        int slot = -1;

        if (preferSlot.isToggled()) {
            int preferredSlot = (int) hotbarSlotPreference.getInput() - 1;
            if (isEnderPearl(preferredSlot)) {
                slot = preferredSlot;
            }
        } else {
            for (int i = 0; i <= 8; i++) {
                if (isEnderPearl(i)) {
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

    private boolean isEnderPearl(int slot) {
        ItemStack item = mc.thePlayer.inventory.getStackInSlot(slot);
        return item != null && "ender pearl".equalsIgnoreCase(item.getDisplayName());
    }
}
