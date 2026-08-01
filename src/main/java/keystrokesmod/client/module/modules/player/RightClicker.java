package keystrokesmod.client.module.modules.player;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PrePlayerTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.player.ClickManager;
import keystrokesmod.client.util.player.JitterHandler;
import keystrokesmod.client.util.system.ReflectUtil;
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

@ModuleInfo(name = "RightClicker", category = Category.Player)
public class RightClicker extends Mod {
    private final RangeValue rightCPS = new RangeValue("RightCPS", this, 12.0, 16.0, 1.0, 60.0, 0.5);
    private final NumberValue jitterRight = new NumberValue("Jitter right", this, 0.0, 0.0, 3.0, 0.1);
    private final NumberValue rightClickDelay = new NumberValue("Rightclick delay (ms)", this, 85.0, 0.0, 500.0, 1.0);
    private final BooleanValue noBlockSword = new BooleanValue("Don't rightclick sword", this, true);
    private final BooleanValue ignoreRods = new BooleanValue("Ignore rods", this, true);
    private final BooleanValue onlyBlocks = new BooleanValue("Only rightclick with blocks", this, false);
    private final BooleanValue preferFastPlace = new BooleanValue("Prefer fast place", this, false);
    private final BooleanValue allowEat = new BooleanValue("Allow eat & drink", this, true);
    private final BooleanValue allowBow = new BooleanValue("Allow bow", this, true);
    private final ModeValue clickTimings = new ModeValue("Click event", this, "Render", "Render", "Tick");

    private final ClickManager clickManager = new ClickManager(this);

    private boolean rightClickWaiting = false;
    private double rightClickWaitStartTime;
    private boolean allowedClick;
    
    @Override
    public void onEnable() {
        this.rightClickWaiting = false;
        this.allowedClick = false;
    }
    
    @Override
    public void onDisable() {
        this.rightClickWaiting = false;
    }
    
    @EventLink
    private Listener<PostRenderTickEvent> render = ev -> {
        if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            return;
        }
        if (clickTimings.is("Render")) {
            this.demiseRightClick();
        }
    };

    @EventLink
    private Listener<PrePlayerTickEvent> prePlayerTick = ev -> {
        if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            return;
        }
        if (clickTimings.is("Tick")) {
            this.demiseRightClick();
        }
    };
    
    private void demiseRightClick() {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null || !mc.inGameHasFocus) {
            return;
        }
        Mouse.poll();
        if (Mouse.isButtonDown(1)) {
            if (!this.rightClickAllowed()) {
                return;
            }
            if (jitterRight.getValue() > 0.0) {
                float[] jitter = JitterHandler.calculateJitter((float) jitterRight.getValue(), true);
                mc.thePlayer.rotationYaw += jitter[0];
                mc.thePlayer.rotationPitch += jitter[1];
            }
            ReflectUtil.rightClickMouse();
        } else {
            this.rightClickWaiting = false;
            this.allowedClick = false;
        }
    }
    
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
        if (rightClickDelay.getValue() != 0.0) {
            if (!this.rightClickWaiting && !this.allowedClick) {
                this.rightClickWaitStartTime = (double)System.currentTimeMillis();
                this.rightClickWaiting = true;
                return false;
            }
            if (this.rightClickWaiting && !this.allowedClick) {
                final double passedTime = System.currentTimeMillis() - this.rightClickWaitStartTime;
                if (passedTime >= rightClickDelay.getValue()) {
                    this.allowedClick = true;
                    this.rightClickWaiting = false;
                    return true;
                }
                return false;
            }
        }
        return true;
    }
}
