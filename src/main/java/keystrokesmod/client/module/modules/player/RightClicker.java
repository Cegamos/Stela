package keystrokesmod.client.module.modules.player;

import java.util.Random;

import org.lwjgl.input.Mouse;

import io.netty.util.internal.ThreadLocalRandom;
import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemBow;
import net.minecraft.item.ItemBucketMilk;
import net.minecraft.item.ItemFishingRod;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemPotion;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

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
    private final ModeValue clickStyle = new ModeValue("Click Style", this, "Raven", "Raven", "Skid");

    private Random rand = null;
    private long righti;
    private long rightj;
    private long rightk;
    private long rightl;
    private double rightm;
    private boolean rightn;
    private long lastClick;
    private long rightHold;
    private boolean rightClickWaiting = false;
    private double rightClickWaitStartTime;
    private boolean allowedClick;
    private boolean rightDown;
    
    @Override
    public void onEnable() {
        this.rightClickWaiting = false;
        this.allowedClick = false;
        this.rand = new Random();
    }
    
    @Override
    public void onDisable() {
        this.rightClickWaiting = false;
    }
    
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent ev) {
        if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            return;
        }
        if (!clickTimings.is("Render")) {
            return;
        }
        if (clickStyle.is("Raven")) {
            this.ravenClick();
        }
        else if (clickStyle.is("Skid")) {
            this.skidClick(ev, null);
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent.PlayerTickEvent ev) {
        if (!Utils.Client.currentScreenMinecraft() && !(Minecraft.getMinecraft().currentScreen instanceof GuiInventory) && !(Minecraft.getMinecraft().currentScreen instanceof GuiChest)) {
            return;
        }
        if (!clickTimings.is("Tick")) {
            return;
        }
        if (clickStyle.is("Raven")) {
            this.ravenClick();
        }
        else if (clickStyle.is("Skid")) {
            this.skidClick(null, ev);
        }
    }
    
    private void skidClick(final TickEvent.RenderTickEvent er, final TickEvent.PlayerTickEvent e) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            return;
        }
        final double speedRight = 1.0 / ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.2, rightCPS.getInputMax());
        final double rightHoldLength = speedRight / ThreadLocalRandom.current().nextDouble(rightCPS.getInputMin() - 0.02, rightCPS.getInputMax());
        if (!Mouse.isButtonDown(1) && !this.rightDown) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
            Utils.Client.setMouseButtonState(1, false);
        }
        if (Mouse.isButtonDown(1) || this.rightDown) {
            if (!this.rightClickAllowed()) {
                return;
            }
            if (jitterRight.getInput() > 0.0) {
                final double jitterMultiplier = jitterRight.getInput() * 0.45;
                if (this.rand.nextBoolean()) {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationYaw += (float)(this.rand.nextFloat() * jitterMultiplier);
                }
                else {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationYaw -= (float)(this.rand.nextFloat() * jitterMultiplier);
                }
                if (this.rand.nextBoolean()) {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationPitch += (float)(this.rand.nextFloat() * jitterMultiplier * 0.45);
                }
                else {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationPitch -= (float)(this.rand.nextFloat() * jitterMultiplier * 0.45);
                }
            }
            if (System.currentTimeMillis() - this.lastClick > speedRight * 1000.0) {
                this.lastClick = System.currentTimeMillis();
                if (this.rightHold < this.lastClick) {
                    this.rightHold = this.lastClick;
                }
                final int key = mc.gameSettings.keyBindUseItem.getKeyCode();
                KeyBinding.setKeyBindState(key, true);
                Utils.Client.setMouseButtonState(1, true);
                KeyBinding.onTick(key);
                this.rightDown = false;
            }
            else if (System.currentTimeMillis() - this.rightHold > rightHoldLength * 1000.0) {
                this.rightDown = true;
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindUseItem.getKeyCode(), false);
                Utils.Client.setMouseButtonState(1, false);
            }
        }
        else if (!Mouse.isButtonDown(1)) {
            this.rightClickWaiting = false;
            this.allowedClick = false;
        }
    }
    
    private void ravenClick() {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            return;
        }
        Mouse.poll();
        if (Mouse.isButtonDown(1)) {
            this.rightClickExecute(mc.gameSettings.keyBindUseItem.getKeyCode());
        }
        else if (!Mouse.isButtonDown(1)) {
            this.rightClickWaiting = false;
            this.allowedClick = false;
            this.righti = 0L;
            this.rightj = 0L;
        }
    }
    
    public boolean rightClickAllowed() {
        final ItemStack item = mc.thePlayer.getHeldItem();
        if (item != null) {
            if (allowEat.isToggled() && (item.getItem() instanceof ItemFood || item.getItem() instanceof ItemPotion || item.getItem() instanceof ItemBucketMilk)) {
                return false;
            }
            if (ignoreRods.isToggled() && item.getItem() instanceof ItemFishingRod) {
                return false;
            }
            if (allowBow.isToggled() && item.getItem() instanceof ItemBow) {
                return false;
            }
            if (onlyBlocks.isToggled() && !(item.getItem() instanceof ItemBlock)) {
                return false;
            }
            if (noBlockSword.isToggled() && item.getItem() instanceof ItemSword) {
                return false;
            }
        }
        if (preferFastPlace.isToggled()) {
            final Mod fastplace = Raven.moduleManager.getModuleByClazz(FastPlace.class);
            if (fastplace != null && fastplace.isEnabled()) {
                return false;
            }
        }
        if (rightClickDelay.getInput() != 0.0) {
            if (!this.rightClickWaiting && !this.allowedClick) {
                this.rightClickWaitStartTime = (double)System.currentTimeMillis();
                this.rightClickWaiting = true;
                return false;
            }
            if (this.rightClickWaiting && !this.allowedClick) {
                final double passedTime = System.currentTimeMillis() - this.rightClickWaitStartTime;
                if (passedTime >= rightClickDelay.getInput()) {
                    this.allowedClick = true;
                    this.rightClickWaiting = false;
                    return true;
                }
                return false;
            }
        }
        return true;
    }
    
    public void rightClickExecute(final int key) {
        if (!this.rightClickAllowed()) {
            return;
        }
        if (jitterRight.getInput() > 0.0) {
            final double jitterMultiplier = jitterRight.getInput() * 0.45;
            if (this.rand.nextBoolean()) {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationYaw += (float)(this.rand.nextFloat() * jitterMultiplier);
            }
            else {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationYaw -= (float)(this.rand.nextFloat() * jitterMultiplier);
            }
            if (this.rand.nextBoolean()) {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationPitch += (float)(this.rand.nextFloat() * jitterMultiplier * 0.45);
            }
            else {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationPitch -= (float)(this.rand.nextFloat() * jitterMultiplier * 0.45);
            }
        }
        if (this.rightj > 0L && this.righti > 0L) {
            if (System.currentTimeMillis() > this.rightj) {
                KeyBinding.setKeyBindState(key, true);
                KeyBinding.onTick(key);
                Utils.Client.setMouseButtonState(1, false);
                Utils.Client.setMouseButtonState(1, true);
                this.genRightTimings();
            }
            else if (System.currentTimeMillis() > this.righti) {
                KeyBinding.setKeyBindState(key, false);
            }
        }
        else {
            this.genRightTimings();
        }
    }
    
    public void genRightTimings() {
        final double clickSpeed = Utils.Client.ranModuleVal(rightCPS, this.rand) + 0.4 * this.rand.nextDouble();
        long delay = (int)Math.round(1000.0 / clickSpeed);
        if (System.currentTimeMillis() > this.rightk) {
            if (!this.rightn && this.rand.nextInt(100) >= 85) {
                this.rightn = true;
                this.rightm = 1.1 + this.rand.nextDouble() * 0.15;
            }
            else {
                this.rightn = false;
            }
            this.rightk = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }
        if (this.rightn) {
            delay *= (long)this.rightm;
        }
        if (System.currentTimeMillis() > this.rightl) {
            if (this.rand.nextInt(100) >= 80) {
                delay += 50L + this.rand.nextInt(100);
            }
            this.rightl = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }
        this.rightj = System.currentTimeMillis() + delay;
        this.righti = System.currentTimeMillis() + delay / 2L - this.rand.nextInt(10);
    }
}
