package keystrokesmod.client.module.modules.player;

import java.util.concurrent.ThreadLocalRandom;

import org.lwjgl.input.Mouse;

import keystrokesmod.client.Raven;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.combat.LeftClicker;
import keystrokesmod.client.module.value.impl.RangeValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockLiquid;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemShears;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemTool;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "AutoTool", category = Category.Player)
public class AutoTool extends Mod {
    private final BooleanValue hotkeyBack = new BooleanValue("Hotkey back", this, true);
    private final RangeValue mineDelay = new RangeValue("Max delay", this, 10.0, 50.0, 0.0, 2000.0, 1.0);
    private Block previousBlock;
    private boolean isWaiting;
    public static int previousSlot;
    public static boolean justFinishedMining;
    public static boolean mining;
    public static Clock delay = new Clock(0L);

    @SubscribeEvent
    public void onRenderTick(final TickEvent.RenderTickEvent e) {
        if (!Utils.Player.isPlayerInGame() || mc.currentScreen != null) {
            return;
        }
        if (!Mouse.isButtonDown(0)) {
            if (mining) {
                this.finishMining();
            }
            if (this.isWaiting) {
                this.isWaiting = false;
            }
            return;
        }
        final LeftClicker autoClicker = (LeftClicker)Raven.moduleManager.getModuleByClazz(LeftClicker.class);
        if (autoClicker.isEnabled() && !autoClicker.breakBlocks.isToggled()) {
            return;
        }
        final BlockPos lookingAtBlock = mc.objectMouseOver.getBlockPos();
        if (lookingAtBlock != null) {
            final Block stateBlock = mc.theWorld.getBlockState(lookingAtBlock).getBlock();
            if (stateBlock != Blocks.air && !(stateBlock instanceof BlockLiquid) && stateBlock instanceof Block) {
                if (mineDelay.getInputMax() > 0.0) {
                    if (this.previousBlock != null) {
                        if (this.previousBlock != stateBlock) {
                            this.previousBlock = stateBlock;
                            this.isWaiting = true;
                            delay.setCooldown((long)ThreadLocalRandom.current().nextDouble(mineDelay.getInputMin(), mineDelay.getInputMax() + 0.01));
                            delay.start();
                        }
                        else if (this.isWaiting && delay.hasFinished()) {
                            this.isWaiting = false;
                            previousSlot = Utils.Player.getCurrentPlayerSlot();
                            mining = true;
                            this.hotkeyToFastest();
                        }
                    }
                    else {
                        this.previousBlock = stateBlock;
                        this.isWaiting = false;
                    }
                    return;
                }
                if (!mining) {
                    previousSlot = Utils.Player.getCurrentPlayerSlot();
                    mining = true;
                }
                this.hotkeyToFastest();
            }
        }
    }
    
    public void finishMining() {
        if (this.hotkeyBack.isToggled()) {
            Utils.Player.hotkeyToSlot(previousSlot);
        }
        justFinishedMining = false;
        mining = false;
    }
    
    private void hotkeyToFastest() {
        int index = -1;
        double speed = 1.0;
        for (int slot = 0; slot <= 8; ++slot) {
            final ItemStack itemInSlot = mc.thePlayer.inventory.getStackInSlot(slot);
            if (itemInSlot != null && (itemInSlot.getItem() instanceof ItemTool || itemInSlot.getItem() instanceof ItemShears)) {
                final BlockPos p = mc.objectMouseOver.getBlockPos();
                final Block bl = mc.theWorld.getBlockState(p).getBlock();
                if (itemInSlot.getItem().getDigSpeed(itemInSlot, bl.getDefaultState()) > speed) {
                    speed = itemInSlot.getItem().getDigSpeed(itemInSlot, bl.getDefaultState());
                    index = slot;
                }
            }
        }
        if (index != -1 && speed > 1.1) {
            if (speed != 0.0) {
                Utils.Player.hotkeyToSlot(index);
            }
        }
    }
}
