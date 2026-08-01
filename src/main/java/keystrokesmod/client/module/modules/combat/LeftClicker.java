package keystrokesmod.client.module.modules.combat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

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
import keystrokesmod.client.util.player.ClickManager;
import keystrokesmod.client.util.player.JitterHandler;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
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

    private final ClickManager clickManager = new ClickManager(this);
    private boolean breakHeld;

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
        if (clickTimings.is("Render")) {
            this.demiseClick();
        }
    };

    @EventLink
    private Listener<PrePlayerTickEvent> prePlayerTick = event -> {
        if (!Utils.Client.currentScreenMinecraft() && !(mc.currentScreen instanceof GuiInventory) && !(mc.currentScreen instanceof GuiChest)) {
            return;
        }
        if (clickTimings.is("Tick")) {
            this.demiseClick();
        }
    };

    private void demiseClick() {
        if (mc.currentScreen != null || !mc.inGameHasFocus) {
            this.doInventoryClick();
            return;
        }
        Mouse.poll();
        if (Mouse.isButtonDown(0)) {
            if (this.breakBlock()) return;
            if (weaponOnly.getValue() && !Utils.Player.isPlayerHoldingWeapon()) return;
            if (jitterLeft.getValue() > 0.0) {
                float[] jitter = JitterHandler.calculateJitter((float) jitterLeft.getValue(), true);
                mc.thePlayer.rotationYaw += jitter[0];
                mc.thePlayer.rotationPitch += jitter[1];
            }
            clickManager.click(3.0f, null);
        }
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
            if (Mouse.isButtonDown(0) && (Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42))) {
                int x = Mouse.getX() * mc.currentScreen.width / mc.displayWidth;
                int y = mc.currentScreen.height - Mouse.getY() * mc.currentScreen.height / mc.displayHeight - 1;
                ReflectUtil.mouseClicked(x, y, 0);
            }
        }
    }
}
