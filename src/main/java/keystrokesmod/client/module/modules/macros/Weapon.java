package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.*;
import keystrokesmod.client.util.*;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.item.ItemStack;

import java.util.Collection;

@ModuleInfo(name = "Weapon", category = Category.Macros)
public class Weapon extends Mod {

    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            this.disable();
            return;
        }

        int bestSlot = -1;
        double highestDamage = Double.NEGATIVE_INFINITY;

        for (int slot = 0; slot <= 8; slot++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(slot);
            if (stack == null) continue;

            Collection<AttributeModifier> modifiers = stack.getAttributeModifiers().values();
            for (AttributeModifier modifier : modifiers) {
                double amount = modifier.getAmount();
                if (amount > highestDamage) {
                    highestDamage = amount;
                    bestSlot = slot;
                }
            }
        }

        if (bestSlot != -1 && mc.thePlayer.inventory.currentItem != bestSlot) {
            Utils.Player.hotkeyToSlot(bestSlot);
        }

        this.onDisable();
        this.disable();
    }
}
