package keystrokesmod.client.module.modules.combat;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "AutoWeapon", category = Category.Combat)
public class AutoWeapon extends Mod {
    public BooleanValue onlyWhenHoldingDown = new BooleanValue("Only when holding lmb", this, true);
    public BooleanValue goBackToPrevSlot = new BooleanValue("Revert to old slot", this, true);
    private boolean onWeapon;
    private int prevSlot;
    
    @SubscribeEvent
    public void datsDaSoundOfDaPolis(final TickEvent.RenderTickEvent ev) {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
            return;
        }
        if (mc.objectMouseOver == null || mc.objectMouseOver.entityHit == null || (onlyWhenHoldingDown.getValue() && !Mouse.isButtonDown(0))) {
            if (this.onWeapon) {
                this.onWeapon = false;
                if (goBackToPrevSlot.getValue()) {
                    mc.thePlayer.inventory.currentItem = this.prevSlot;
                }
            }
        }
        else {
            final Entity target = mc.objectMouseOver.entityHit;
            if (onlyWhenHoldingDown.getValue() && !Mouse.isButtonDown(0)) {
                return;
            }
            if (!this.onWeapon) {
                this.prevSlot = mc.thePlayer.inventory.currentItem;
                this.onWeapon = true;
                final int maxDamageSlot = Utils.Player.getMaxDamageSlot();
                if (maxDamageSlot > 0 && Utils.Player.getSlotDamage(maxDamageSlot) > Utils.Player.getSlotDamage(mc.thePlayer.inventory.currentItem)) {
                    mc.thePlayer.inventory.currentItem = maxDamageSlot;
                }
            }
        }
    }
}
