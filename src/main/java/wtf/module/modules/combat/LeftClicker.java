package wtf.module.modules.combat;

import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiInventory;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import wtf.Kevin;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.GameEvent;
import wtf.event.impl.PreTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.DescriptionValue;
import wtf.util.Utils;
import wtf.util.player.ClickManager;
import wtf.util.system.ReflectUtil;

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