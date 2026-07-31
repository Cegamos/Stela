package keystrokesmod.client.module.modules.macros;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;

@ModuleInfo(name = "Armor", category = Category.Macros)
public class Armour extends Mod {
    public BooleanValue ignoreIfAlreadyEquipped = new BooleanValue("Ignore if already equipped", this, true);
    
    @Override
    public void onEnable() {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        int index = -1;
        double strength = -1.0;
        for (int armorType = 0; armorType < 4; ++armorType) {
            index = -1;
            strength = -1.0;
            for (int slot = 0; slot <= 8; ++slot) {
                final ItemStack itemStack = mc.thePlayer.inventory.getStackInSlot(slot);
                if (itemStack != null && itemStack.getItem() instanceof ItemArmor) {
                    final ItemArmor armorPiece = (ItemArmor)itemStack.getItem();
                    if (!Utils.Player.playerWearingArmor().contains(armorPiece.armorType) && armorPiece.armorType == armorType && ignoreIfAlreadyEquipped.isToggled()) {
                        if (armorPiece.getArmorMaterial().getDamageReductionAmount(armorType) > strength) {
                            strength = armorPiece.getArmorMaterial().getDamageReductionAmount(armorType);
                            index = slot;
                        }
                    }
                    else if (Utils.Player.playerWearingArmor().contains(armorPiece.armorType) && armorPiece.armorType == armorType && !ignoreIfAlreadyEquipped.isToggled()) {
                        ItemArmor playerArmor;
                        if (armorType == 0) {
                            playerArmor = (ItemArmor)mc.thePlayer.getCurrentArmor(3).getItem();
                        }
                        else if (armorType == 1) {
                            playerArmor = (ItemArmor)mc.thePlayer.getCurrentArmor(2).getItem();
                        }
                        else if (armorType == 2) {
                            playerArmor = (ItemArmor)mc.thePlayer.getCurrentArmor(1).getItem();
                        }
                        else {
                            if (armorType != 3) {
                                continue;
                            }
                            playerArmor = (ItemArmor)mc.thePlayer.getCurrentArmor(0).getItem();
                        }
                        if (armorPiece.getArmorMaterial().getDamageReductionAmount(armorType) > strength && armorPiece.getArmorMaterial().getDamageReductionAmount(armorType) > playerArmor.getArmorMaterial().getDamageReductionAmount(armorType)) {
                            strength = armorPiece.getArmorMaterial().getDamageReductionAmount(armorType);
                            index = slot;
                        }
                    }
                    else if (!Utils.Player.playerWearingArmor().contains(armorPiece.armorType) && armorPiece.armorType == armorType && !ignoreIfAlreadyEquipped.isToggled() && armorPiece.getArmorMaterial().getDamageReductionAmount(armorType) > strength) {
                        strength = armorPiece.getArmorMaterial().getDamageReductionAmount(armorType);
                        index = slot;
                    }
                }
            }
            if (index > -1 || strength > -1.0) {
                mc.thePlayer.inventory.currentItem = index;
                this.disable();
                this.onDisable();
                return;
            }
        }
        this.onDisable();
        this.disable();
    }
}
