package keystrokesmod.client.module.modules.combat;

import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import io.netty.util.internal.ThreadLocalRandom;
import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PrePlayerTickEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.ModeValue;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.KeyBinding;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "LeftClicker", category = Category.Combat)
public class LeftClicker extends Mod {
	protected final DescriptionValue desc = new DescriptionValue("Best with delay remover", this);
	public RangeValue leftCPS = new RangeValue("Left CPS", this, 9.0, 13.0, 1.0, 60.0, 0.5);
	public NumberValue jitterLeft = new NumberValue("Jitter left", this, 0.0, 0.0, 3.0, 0.1);
	public BooleanValue inventoryFill = new BooleanValue("Inventory fill", this, false);
	public BooleanValue weaponOnly = new BooleanValue("Weapon only", this, false);
	public BooleanValue breakBlocks = new BooleanValue("Break blocks", this, false);
	public ModeValue clickTimings = new ModeValue("Click event", this, "Render", "Render", "Tick");
	public ModeValue clickStyle = new ModeValue("Click Style", this, "Raven", "Raven", "Skid");

    private long lastClick;
    private long leftHold;
    public static boolean breakTimeDone;
    private boolean leftDown;
    private long leftDownTime;
    private long leftUpTime;
    private long leftk;
    private long leftl;
    private double leftm;
    private boolean leftn;
    private boolean breakHeld;
    private Random rand;

    @Override
    public void onEnable() {
    	super.onEnable();
        this.rand = new Random();
    }
    
    @Override
    public void onDisable() {
    	super.onDisable();
        this.leftDownTime = 0L;
        this.leftUpTime = 0L;
    }
    
    @EventLink
    private Listener<PreTickEvent> preTick = event -> {
        final Reach reach = (Reach) Raven.moduleManager.getModuleByClazz(Reach.class);
        if (!Mouse.isButtonDown(0) || !reach.call()) {
            mc.entityRenderer.getMouseOver(1.0f);
        }
    };
    
    @EventLink
    private Listener<PostRenderTickEvent> render = event -> {
        if (!Utils.Client.currentScreenMinecraft() && !(mc.currentScreen instanceof GuiInventory) && !(mc.currentScreen instanceof GuiChest)) {
            return;
        }
        if (!clickTimings.is("Render")) {
            return;
        }
        if (clickStyle.is("Raven")) {
            this.ravenClick();
        }
        else if (clickStyle.is("Skid")) {
            this.skidClick(event, null);
        }
    };


    @EventLink
    private Listener<PrePlayerTickEvent> prePlayerTick = event -> {
        if (!Utils.Client.currentScreenMinecraft() && !(mc.currentScreen instanceof GuiInventory) && !(mc.currentScreen instanceof GuiChest)) {
            return;
        }
        if (!clickTimings.is("Tick")) {
            return;
        }
        if (clickStyle.is("Raven")) {
            this.ravenClick();
        }
        else if (clickStyle.is("Skid")) {
            this.skidClick(null, event);
        }
    };
    
    private void skidClick(final PostRenderTickEvent er, final PrePlayerTickEvent e) {
        if (!Utils.Player.isPlayerInGame()) {
            return;
        }
        final double speedLeft1 = 1.0 / ThreadLocalRandom.current().nextDouble(leftCPS.getInputMin() - 0.2, leftCPS.getInputMax());
        final double leftHoldLength = speedLeft1 / ThreadLocalRandom.current().nextDouble(leftCPS.getInputMin() - 0.02, leftCPS.getInputMax());
        Mouse.poll();
        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            this.doInventoryClick();
            return;
        }
        if (Mouse.isButtonDown(0)) {
            if (this.breakBlock()) {
                return;
            }
            if (weaponOnly.getValue() && !Utils.Player.isPlayerHoldingWeapon()) {
                return;
            }
            if (jitterLeft.getValue() > 0.0) {
                final double a = jitterLeft.getValue() * 0.45;
                if (this.rand.nextBoolean()) {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationYaw += (float)(this.rand.nextFloat() * a);
                }
                else {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationYaw -= (float)(this.rand.nextFloat() * a);
                }
                if (this.rand.nextBoolean()) {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationPitch += (float)(this.rand.nextFloat() * a * 0.45);
                }
                else {
                    final EntityPlayerSP entityPlayer = mc.thePlayer;
                    entityPlayer.rotationPitch -= (float)(this.rand.nextFloat() * a * 0.45);
                }
            }
            final double speedLeft2 = 1.0 / java.util.concurrent.ThreadLocalRandom.current().nextDouble(leftCPS.getInputMin() - 0.2, leftCPS.getInputMax());
            if (System.currentTimeMillis() - this.lastClick > speedLeft2 * 1000.0) {
                this.lastClick = System.currentTimeMillis();
                if (this.leftHold < this.lastClick) {
                    this.leftHold = this.lastClick;
                }
                final int key = mc.gameSettings.keyBindAttack.getKeyCode();
                KeyBinding.setKeyBindState(key, true);
                KeyBinding.onTick(key);
                Utils.Client.setMouseButtonState(0, true);
            }
            else if (System.currentTimeMillis() - this.leftHold > leftHoldLength * 1000.0) {
                KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
                Utils.Client.setMouseButtonState(0, false);
            }
        }
    }
    
    private void ravenClick() {
        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            this.doInventoryClick();
            return;
        }
        Mouse.poll();
        if (!Mouse.isButtonDown(0) && !this.leftDown) {
            KeyBinding.setKeyBindState(mc.gameSettings.keyBindAttack.getKeyCode(), false);
            Utils.Client.setMouseButtonState(0, false);
        }
        if (Mouse.isButtonDown(0) || this.leftDown) {
            if (weaponOnly.getValue() && !Utils.Player.isPlayerHoldingWeapon()) {
                return;
            }
            this.leftClickExecute(mc.gameSettings.keyBindAttack.getKeyCode());
        }
    }
    
    public void leftClickExecute(final int key) {
        if (this.breakBlock()) {
            return;
        }
        if (jitterLeft.getValue() > 0.0) {
            final double a = jitterLeft.getValue() * 0.45;
            if (this.rand.nextBoolean()) {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationYaw += (float)(this.rand.nextFloat() * a);
            }
            else {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationYaw -= (float)(this.rand.nextFloat() * a);
            }
            if (this.rand.nextBoolean()) {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationPitch += (float)(this.rand.nextFloat() * a * 0.45);
            }
            else {
                final EntityPlayerSP entityPlayer = mc.thePlayer;
                entityPlayer.rotationPitch -= (float)(this.rand.nextFloat() * a * 0.45);
            }
        }
        if (this.leftUpTime > 0L && this.leftDownTime > 0L) {
            if (System.currentTimeMillis() > this.leftUpTime && this.leftDown) {
                KeyBinding.setKeyBindState(key, true);
                KeyBinding.onTick(key);
                this.genLeftTimings();
                Utils.Client.setMouseButtonState(0, true);
                this.leftDown = false;
            }
            else if (System.currentTimeMillis() > this.leftDownTime) {
                KeyBinding.setKeyBindState(key, false);
                this.leftDown = true;
                Utils.Client.setMouseButtonState(0, false);
            }
        }
        else {
            this.genLeftTimings();
        }
    }
    
    public void genLeftTimings() {
        final double clickSpeed = Utils.Client.ranModuleVal(leftCPS, this.rand) + 0.4 * this.rand.nextDouble();
        long delay = (int)Math.round(1000.0 / clickSpeed);
        if (System.currentTimeMillis() > this.leftk) {
            if (!this.leftn && this.rand.nextInt(100) >= 85) {
                this.leftn = true;
                this.leftm = 1.1 + this.rand.nextDouble() * 0.15;
            }
            else {
                this.leftn = false;
            }
            this.leftk = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }
        if (this.leftn) {
            delay *= (long)this.leftm;
        }
        if (System.currentTimeMillis() > this.leftl) {
            if (this.rand.nextInt(100) >= 80) {
                delay += 50L + this.rand.nextInt(100);
            }
            this.leftl = System.currentTimeMillis() + 500L + this.rand.nextInt(1500);
        }
        this.leftUpTime = System.currentTimeMillis() + delay;
        this.leftDownTime = System.currentTimeMillis() + delay / 2L - this.rand.nextInt(10);
    }
    
    public boolean breakBlock() {
        if (breakBlocks.getValue() && mc.objectMouseOver != null) {
            final BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null) {
                final Block bl = mc.theWorld.getBlockState(p).getBlock();
                if (bl != Blocks.air && !(bl instanceof BlockLiquid)) {
                    if (!this.breakHeld) {
                        final int e = mc.gameSettings.keyBindAttack.getKeyCode();
                        KeyBinding.setKeyBindState(e, true);
                        KeyBinding.onTick(e);
                        this.breakHeld = true;
                    }
                    return true;
                }
                if (this.breakHeld) {
                    this.breakHeld = false;
                }
            }
        }
        return false;
    }
    
    public void doInventoryClick() {
        if (inventoryFill.getValue() && (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest)) {
            if (!Mouse.isButtonDown(0) || (!Keyboard.isKeyDown(54) && !Keyboard.isKeyDown(42))) {
                this.leftDownTime = 0L;
                this.leftUpTime = 0L;
            }
            else if (this.leftDownTime != 0L && this.leftUpTime != 0L) {
                if (System.currentTimeMillis() > this.leftUpTime) {
                    this.genLeftTimings();
                    int x = Mouse.getX() * mc.currentScreen.width / mc.displayWidth;
                    int y = mc.currentScreen.height - Mouse.getY() * mc.currentScreen.height / mc.displayHeight - 1;
                    ReflectUtil.mouseClicked(x, y, 0);
                }
            }
            else {
                this.genLeftTimings();
            }
        }
    }
}
