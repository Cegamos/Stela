package wtf.module.modules.player;

import org.lwjgl.input.Mouse;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import wtf.Kevin;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.GameEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.RangeValue;
import wtf.util.Utils;
import wtf.util.system.ReflectUtil;

@ModuleInfo(name = "RightClicker", category = Category.Player)
public class RightClicker extends Mod {
    private final RangeValue rightCPS = new RangeValue("CPS", this, 12.0, 16.0, 1.0, 60.0, 0.5);
    private final BooleanValue noBlockSword = new BooleanValue("Don't rightclick sword", this, true);
    private final BooleanValue ignoreRods = new BooleanValue("Ignore rods", this, true);
    private final BooleanValue onlyBlocks = new BooleanValue("Only rightclick with blocks", this, false);
    private final BooleanValue preferFastPlace = new BooleanValue("Prefer fast place", this, false);
    private final BooleanValue allowEat = new BooleanValue("Allow eat & drink", this, true);
    private final BooleanValue allowBow = new BooleanValue("Allow bow", this, true);

    @EventLink
    private Listener<GameEvent> game = ev -> {
        if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            return;
        }
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null || !mc.inGameHasFocus) {
            return;
        }
        Mouse.poll();
        if (Mouse.isButtonDown(1)) {
            if (!this.rightClickAllowed()) {
                return;
            }

            ReflectUtil.rightClickMouse();
        }
    };
    
    public boolean rightClickAllowed() {
        final ItemStack item = mc.thePlayer.getHeldItem();
        if (item != null) {
            if (allowEat.getValue() && (item.getItem() instanceof ItemFood || item.getItem() instanceof ItemPotion || item.getItem() instanceof ItemBucketMilk)) {
                return false;
            }
            if (ignoreRods.getValue() && item.getItem() instanceof ItemFishingRod) {
                return false;
            }
            if (allowBow.getValue() && item.getItem() instanceof ItemBow) {
                return false;
            }
            if (onlyBlocks.getValue() && !(item.getItem() instanceof ItemBlock)) {
                return false;
            }
            if (noBlockSword.getValue() && item.getItem() instanceof ItemSword) {
                return false;
            }
        }
        if (preferFastPlace.getValue()) {
            final Mod fastplace = Kevin.moduleManager.getModuleByClazz(FastPlace.class);
            if (fastplace != null && fastplace.isEnabled()) {
                return false;
            }
        }
        return true;
    }
}
