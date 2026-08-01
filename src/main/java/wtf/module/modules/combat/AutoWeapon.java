package wtf.module.modules.combat;

import org.lwjgl.input.Mouse;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PostRenderTickEvent;
import wtf.event.impl.PreRenderTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.util.Utils;

@ModuleInfo(name = "AutoWeapon", category = Category.Combat)
public class AutoWeapon extends Mod {
	public BooleanValue onlyWhenHoldingDown = new BooleanValue("Only when holding lmb", this, true);
	public BooleanValue goBackToPrevSlot = new BooleanValue("Revert to old slot", this, true);
	private boolean onWeapon;
	private int prevSlot;

	@EventLink
	private Listener<PreRenderTickEvent> preRenderTick = event -> both();

	@EventLink
	private Listener<PostRenderTickEvent> postRenderTick = event -> both();

	private void both() {
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
		} else {
			if (onlyWhenHoldingDown.getValue() && !Mouse.isButtonDown(0)) {
				return;
			}
			if (!this.onWeapon) {
				this.prevSlot = mc.thePlayer.inventory.currentItem;
				this.onWeapon = true;
				final int maxDamageSlot = Utils.Player.getMaxDamageSlot();
				if (maxDamageSlot > 0 && Utils.Player.getSlotDamage(maxDamageSlot) > Utils.Player
						.getSlotDamage(mc.thePlayer.inventory.currentItem)) {
					mc.thePlayer.inventory.currentItem = maxDamageSlot;
				}
			}
		}
	}
}
