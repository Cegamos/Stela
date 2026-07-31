package keystrokesmod.client.module.modules.other;

import java.awt.AWTException;
import java.awt.Robot;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.combat.AimAssist;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.entity.Entity;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "MiddleClick", category = Category.Other)
public class MiddleClick extends Mod {
	public ModeValue mode = new ModeValue("On click", this, "Add friend", "Addfriend", "Remove Friend", "Throw pearl");
	public BooleanValue showHelp = new BooleanValue("Show friend help in chat", this, true);
	int prevSlot;
	public static boolean a;
	private Robot bot;
	private boolean hasClicked;
	private int pearlEvent;

	@Override
	public void onEnable() {
		try {
			this.bot = new Robot();
		} catch (AWTException var2) {
			this.disable();
		}
		this.hasClicked = false;
		this.pearlEvent = 4;
	}

	@SubscribeEvent
	public void onTick(final TickEvent.PlayerTickEvent e) {
		if (!Utils.Player.isPlayerInGame()) {
			return;
		}
		if (this.pearlEvent < 4) {
			if (this.pearlEvent == 3) {
				mc.thePlayer.inventory.currentItem = this.prevSlot;
			}
			++this.pearlEvent;
		}
		if (Mouse.isButtonDown(2) && !this.hasClicked) {
			if (mode.is("Throw Pearl")) {
				for (int slot = 0; slot <= 8; ++slot) {
					final ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
					if (itemInSlot != null && itemInSlot.getItem() instanceof ItemEnderPearl) {
						this.prevSlot = mc.thePlayer.inventory.currentItem;
						mc.thePlayer.inventory.currentItem = slot;
						this.bot.mousePress(4);
						this.bot.mouseRelease(4);
						this.pearlEvent = 0;
						this.hasClicked = true;
						return;
					}
				}
			} else if (mode.is("Add friend")) {
				this.addFriend();
				if (showHelp.isToggled()) {
					this.showHelpMessage();
				}
			} else if (mode.is("Remove Friend")) {
				removeFriend();
				if (showHelp.isToggled()) {
					this.showHelpMessage();
				}
			}

			this.hasClicked = true;
		} else if (!Mouse.isButtonDown(2) && this.hasClicked) {
			this.hasClicked = false;
		}
	}

	private void showHelpMessage() {
		if (showHelp.isToggled()) {
			Utils.Player.sendMessageToSelf(
					"Run 'help friends' in CommandLine to find out how to add, remove and view friends.");
		}
	}

	private void removeFriend() {
    	AimAssist aim = (AimAssist) Raven.moduleManager.getModuleByClazz(AimAssist.class);

		final Entity player = mc.objectMouseOver.entityHit;
		if (player == null) {
			Utils.Player.sendMessageToSelf("Please aim at a player/entity when removing them.");
		} else if (aim.removeFriend(player)) {
			Utils.Player.sendMessageToSelf("Successfully removed " + player.getName() + " from friends list!");
		} else {
			Utils.Player.sendMessageToSelf(player.getName() + " was not found in the friends list!");
		}
	}

	private void addFriend() {
    	AimAssist aim = (AimAssist) Raven.moduleManager.getModuleByClazz(AimAssist.class);

		final Entity player = mc.objectMouseOver.entityHit;
		if (player == null) {
			Utils.Player.sendMessageToSelf("Please aim at a player/entity when adding them.");
		} else {
			aim.addFriend(player);
			Utils.Player.sendMessageToSelf("Successfully added " + player.getName() + " to friends list.");
		}
	}
}
