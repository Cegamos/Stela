package wtf.module.modules.client;

import org.lwjgl.input.Mouse;

import net.minecraft.client.settings.KeyBinding;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemStack;
import wtf.Kevin;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PreUpdateEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.modules.combat.AimAssist;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.ModeValue;
import wtf.util.Utils;

@ModuleInfo(name = "MiddleClick", category = Category.Client)
public class MiddleClick extends Mod {
    
    public final ModeValue mode = new ModeValue("On click", this, "Add Friend", "Add Friend", "Remove Friend", "Throw Pearl");
    public final BooleanValue showHelp = new BooleanValue("Show friend help in chat", this, true);
    
    private int prevSlot = -1;
    private boolean hasClicked = false;
    private int pearlEvent = 4;
    
    private AimAssist cachedAimAssist = null;

    @Override
    public void onEnable() {
        super.onEnable();
        this.hasClicked = false;
        this.pearlEvent = 4;
        this.prevSlot = -1;
        
        if (cachedAimAssist == null) {
            cachedAimAssist = (AimAssist) Kevin.moduleManager.getModuleByClazz(AimAssist.class);
        }
    }

    @EventLink
    private final Listener<PreUpdateEvent> onPreUpdate = event -> {
        if (checkGame()) return;

        if (this.pearlEvent < 4) {
            if (this.pearlEvent == 3 && this.prevSlot != -1) {
                mc.thePlayer.inventory.currentItem = this.prevSlot;
                this.prevSlot = -1;
            }
            this.pearlEvent++;
        }

        if (Mouse.isButtonDown(2)) {
            if (!this.hasClicked) {
                this.hasClicked = true;
                
                if (mode.is("Throw Pearl")) {
                    executePearlThrow();
                } else if (mode.is("Add Friend")) {
                    addFriend();
                } else if (mode.is("Remove Friend")) {
                    removeFriend();
                }
            }
        } else {
            this.hasClicked = false;
        }
    };

    private void executePearlThrow() {
        for (int slot = 0; slot < 9; slot++) {
            final ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
            
            if (itemInSlot != null && itemInSlot.getItem() instanceof ItemEnderPearl) {
                this.prevSlot = mc.thePlayer.inventory.currentItem;
                mc.thePlayer.inventory.currentItem = slot;
                
                KeyBinding.onTick(mc.gameSettings.keyBindUseItem.getKeyCode());
                
                this.pearlEvent = 0;
                return;
            }
        }
    }

    private void addFriend() {
        final Entity target = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
        
        if (target instanceof EntityPlayer) {
            if (cachedAimAssist != null) {
                cachedAimAssist.addFriend(target.getName());
                Utils.Player.sendMessageToSelf("Successfully added " + target.getName() + " to friends list.");
                showHelpMessage();
            }
        } else {
            Utils.Player.sendMessageToSelf("Please aim at a player when adding them.");
        }
    }

    private void removeFriend() {
        final Entity target = mc.objectMouseOver != null ? mc.objectMouseOver.entityHit : null;
        
        if (target instanceof EntityPlayer) {
            if (cachedAimAssist != null) {
                if (cachedAimAssist.getFriends().contains(target.getName().toLowerCase())) {
                    cachedAimAssist.removeFriend(target.getName());
                    Utils.Player.sendMessageToSelf("Successfully removed " + target.getName() + " from friends list!");
                    showHelpMessage();
                } else {
                    Utils.Player.sendMessageToSelf(target.getName() + " was not found in the friends list!");
                }
            }
        } else {
            Utils.Player.sendMessageToSelf("Please aim at a player when removing them.");
        }
    }

    private void showHelpMessage() {
        if (showHelp.getValue()) {
            Utils.Player.sendMessageToSelf("Run 'help friends' in CommandLine to find out how to add, remove and view friends.");
        }
    }
}