package keystrokesmod.client.module.modules.combat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import keystrokesmod.client.Kevin;
import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.GameEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.player.ClickManager;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;

@ModuleInfo(name = "LeftClicker", category = Category.Combat)
public class LeftClicker extends Mod {
    protected final DescriptionValue desc = new DescriptionValue("Best with delay remover", this);
    private final ClickManager clickManager = new ClickManager(this);
    public BooleanValue inventoryFill = new BooleanValue("Inventory fill", this, false);
    public BooleanValue weaponOnly = new BooleanValue("Weapon only", this, false);
    public BooleanValue breakBlocks = new BooleanValue("Break blocks", this, false);

    private boolean breakHeld;
    private boolean wasDown;

    @EventLink
    private Listener<PreTickEvent> preTick = event -> {
        final Reach reach = (Reach) Kevin.moduleManager.getModuleByClazz(Reach.class);
        if (!Mouse.isButtonDown(0) || !reach.call()) {
            mc.entityRenderer.getMouseOver(1.0f);
        }
    };
    
    @EventLink
    private Listener<GameEvent> render = event -> {
        if (!Utils.Client.currentScreenMinecraft() && !(currentScreen() instanceof GuiInventory) && !(currentScreen() instanceof GuiChest)) {
            return;
        }
        
        if (currentScreen() != null || !mc.inGameHasFocus) {
            this.doInventoryClick();
            wasDown = false; 
            return;
        }
        
        boolean isDown = GameSettings.isKeyDown(gameSetting().keyBindAttack);

        if (isDown) {
            if (this.breakBlock()) {
                wasDown = true;
                return;
            }
            if (weaponOnly.getValue() && !Utils.Player.isPlayerHoldingWeapon()) {
                wasDown = true;
                return;
            }
            
            if (wasDown) {
                ReflectUtil.setLeftClickCounter(0);
                clickManager.click(3, null);
            }
        }
        
        wasDown = isDown;
    };

    public boolean breakBlock() {
        if (breakBlocks.getValue() && mc.objectMouseOver != null) {
            final BlockPos p = mc.objectMouseOver.getBlockPos();
            if (p != null) {
                final Block bl = mc.theWorld.getBlockState(p).getBlock();
                if (bl != Blocks.air && !(bl instanceof BlockLiquid)) {
                    if (!this.breakHeld) {
                        ReflectUtil.setLeftClickCounter(0);
                        clickManager.click(3, null);
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
        if (inventoryFill.getValue() && (currentScreen() instanceof GuiInventory || currentScreen() instanceof GuiChest)) {
            if (Mouse.isButtonDown(0) && (Keyboard.isKeyDown(54) || Keyboard.isKeyDown(42))) {
                int x = Mouse.getX() * currentScreen().width / mc.displayWidth;
                int y = currentScreen().height - Mouse.getY() * currentScreen().height / mc.displayHeight - 1;
                ReflectUtil.mouseClicked(x, y, 0);
            }
        }
    }
}