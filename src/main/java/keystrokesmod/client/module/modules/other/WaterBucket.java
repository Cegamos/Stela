package keystrokesmod.client.module.modules.other;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.DimensionUtil;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "WaterBucket", category = Category.Other)
public class WaterBucket extends Mod {
    private boolean handling;

    @Override
    public boolean canBeEnabled() {
        return !DimensionUtil.isPlayerInNether();
    }
    
    @SubscribeEvent
    public void onTick(final TickEvent.ClientTickEvent ev) {
        if (ev.phase != TickEvent.Phase.END && Utils.Player.isPlayerInGame() && !mc.isGamePaused()) {
            if (DimensionUtil.isPlayerInNether()) {
                this.disable();
            }
            if (this.inPosition() && this.holdWaterBucket()) {
                this.handling = true;
            }
            if (this.handling) {
                this.mlg();
                if (mc.thePlayer.onGround || mc.thePlayer.motionY > 0.0) {
                    this.reset();
                }
            }
        }
    }
    
    private boolean inPosition() {
        if (mc.thePlayer.motionY < -0.6 && !mc.thePlayer.onGround && !mc.thePlayer.capabilities.isFlying && !mc.thePlayer.capabilities.isCreativeMode && !this.handling) {
            final BlockPos playerPos = mc.thePlayer.getPosition();
            for (int i = 1; i < 3; ++i) {
                final BlockPos blockPos = playerPos.down(i);
                final Block block = mc.theWorld.getBlockState(blockPos).getBlock();
                if (block.isBlockSolid((IBlockAccess)mc.theWorld, blockPos, EnumFacing.UP)) {
                    return false;
                }
            }
            return true;
        }
        return false;
    }
    
    private boolean holdWaterBucket() {
        if (this.containsItem(mc.thePlayer.getHeldItem(), Items.water_bucket)) {
            return true;
        }
        for (int i = 0; i < InventoryPlayer.getHotbarSize(); ++i) {
            if (this.containsItem(mc.thePlayer.inventory.mainInventory[i], Items.water_bucket)) {
                mc.thePlayer.inventory.currentItem = i;
                return true;
            }
        }
        return false;
    }
    
    private void mlg() {
        final ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (this.containsItem(heldItem, Items.water_bucket) && mc.thePlayer.rotationPitch >= 70.0f) {
            final MovingObjectPosition object = mc.objectMouseOver;
            if (object.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && object.sideHit == EnumFacing.UP) {
                mc.playerController.sendUseItem((EntityPlayer)mc.thePlayer, (World)mc.theWorld, heldItem);
            }
        }
    }
    
    private void reset() {
        final ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (this.containsItem(heldItem, Items.bucket)) {
            mc.playerController.sendUseItem((EntityPlayer)mc.thePlayer, (World)mc.theWorld, heldItem);
        }
        this.handling = false;
    }
    
    private boolean containsItem(final ItemStack itemStack, final Item item) {
        return itemStack != null && itemStack.getItem() == item;
    }
}
