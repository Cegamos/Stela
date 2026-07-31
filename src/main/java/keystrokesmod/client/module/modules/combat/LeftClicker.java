package keystrokesmod.client.module.modules.combat;

import java.util.Random;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import io.netty.util.internal.ThreadLocalRandom;
import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
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
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "LeftClicker", category = Category.Combat)
public class LeftClicker extends Mod {
	private final DescriptionSetting desc = new DescriptionSetting("Best with delay remover", this);
	public DoubleSliderSetting leftCPS = new DoubleSliderSetting("Left CPS", this, 9.0, 13.0, 1.0, 60.0, 0.5);
	public SliderSetting jitterLeft = new SliderSetting("Jitter left", this, 0.0, 0.0, 3.0, 0.1);
	public TickSetting inventoryFill = new TickSetting("Inventory fill", this, false);
	public TickSetting weaponOnly = new TickSetting("Weapon only", this, false);
	public TickSetting breakBlocks = new TickSetting("Break blocks", this, false);
	public ComboSetting clickTimings = new ComboSetting("Click event", this, "Render", "Render", "Tick");
	public ComboSetting clickStyle = new ComboSetting("Click Style", this, "Raven", "Raven", "Skid");

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
        this.rand = new Random();
    }
    
    @Override
    public void onDisable() {
        this.leftDownTime = 0L;
        this.leftUpTime = 0L;
    }
    
    @SubscribeEvent
    public void onTick(TickEvent event) {
        final Reach reach = (Reach) Raven.moduleManager.getModuleByClazz(Reach.class);
        if (!Mouse.isButtonDown(0) || !reach.call()) {
            mc.entityRenderer.getMouseOver(1.0f);
        }
    }
    
    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent ev) {
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
            this.skidClick(ev, null);
        }
    }

    @SubscribeEvent
    public void onTick(final TickEvent.PlayerTickEvent ev) {
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
            this.skidClick(null, ev);
        }
    }
    
    private void skidClick(final TickEvent.RenderTickEvent er, final TickEvent.PlayerTickEvent e) {
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
            if (weaponOnly.isToggled() && !Utils.Player.isPlayerHoldingWeapon()) {
                return;
            }
            if (jitterLeft.getInput() > 0.0) {
                final double a = jitterLeft.getInput() * 0.45;
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
            if (weaponOnly.isToggled() && !Utils.Player.isPlayerHoldingWeapon()) {
                return;
            }
            this.leftClickExecute(mc.gameSettings.keyBindAttack.getKeyCode());
        }
    }
    
    public void leftClickExecute(final int key) {
        if (this.breakBlock()) {
            return;
        }
        if (jitterLeft.getInput() > 0.0) {
            final double a = jitterLeft.getInput() * 0.45;
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
        if (breakBlocks.isToggled() && mc.objectMouseOver != null) {
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
        if (inventoryFill.isToggled() && (mc.currentScreen instanceof GuiInventory || mc.currentScreen instanceof GuiChest)) {
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
