package keystrokesmod.client.module.modules.render;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Xray", category = Category.Render)
public class Xray extends Mod {

    private final NumberValue range = new NumberValue("Range", this, 20.0, 5.0, 50.0, 1.0);

    private final BooleanValue iron = new BooleanValue("Iron", this, true);
    private final BooleanValue gold = new BooleanValue("Gold", this, true);
    private final BooleanValue diamond = new BooleanValue("Diamond", this, true);
    private final BooleanValue emerald = new BooleanValue("Emerald", this, true);
    private final BooleanValue lapis = new BooleanValue("Lapis", this, true);
    private final BooleanValue redstone = new BooleanValue("Redstone", this, true);
    private final BooleanValue coal = new BooleanValue("Coal", this, true);
    private final BooleanValue spawner = new BooleanValue("Spawner", this, true);

    private final List<BlockPos> renderList = new ArrayList<>();
    private final long scanInterval = 200L;
    private Timer scanTimer;

    @Override
    public void onEnable() {
        renderList.clear();
        scanTimer = new Timer();
        scanTimer.scheduleAtFixedRate(createScanTask(), 0L, scanInterval);
    }

    @Override
    public void onDisable() {
        if (scanTimer != null) {
            scanTimer.cancel();
            scanTimer.purge();
            scanTimer = null;
        }
        renderList.clear();
    }

    private TimerTask createScanTask() {
        return new TimerTask() {
            @Override
            public void run() {
                renderList.clear();

                int scanRange = (int) range.getValue();
                for (int y = scanRange; y >= -scanRange; y--) {
                    for (int x = -scanRange; x <= scanRange; x++) {
                        for (int z = -scanRange; z <= scanRange; z++) {
                            if (!Utils.Player.isPlayerInGame()) continue;

                            BlockPos pos = new BlockPos(
                                mc.thePlayer.posX + x,
                                mc.thePlayer.posY + y,
                                mc.thePlayer.posZ + z
                            );

                            Block block = mc.theWorld.getBlockState(pos).getBlock();
                            if (shouldHighlight(block)) {
                                renderList.add(pos);
                            }
                        }
                    }
                }
            }
        };
    }

    @SubscribeEvent
    public void onRenderWorld(RenderWorldLastEvent event) {
        if (!Utils.Player.isPlayerInGame() || renderList.isEmpty()) return;

        List<BlockPos> currentList = new ArrayList<>(renderList);
        for (BlockPos pos : currentList) {
            drawBlock(pos);
        }
    }

    private void drawBlock(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();
        int[] rgb = getColorForBlock(block);
        if (rgb[0] + rgb[1] + rgb[2] != 0) {
            int color = new Color(rgb[0], rgb[1], rgb[2]).getRGB();
            Utils.HUD.re(pos, color, true);
        }
    }

    private boolean shouldHighlight(Block block) {
        return (iron.getValue() && block.equals(Blocks.iron_ore)) ||
               (gold.getValue() && block.equals(Blocks.gold_ore)) ||
               (diamond.getValue() && block.equals(Blocks.diamond_ore)) ||
               (emerald.getValue() && block.equals(Blocks.emerald_ore)) ||
               (lapis.getValue() && block.equals(Blocks.lapis_ore)) ||
               (redstone.getValue() && block.equals(Blocks.redstone_ore)) ||
               (coal.getValue() && block.equals(Blocks.coal_ore)) ||
               (spawner.getValue() && block.equals(Blocks.mob_spawner));
    }

    private int[] getColorForBlock(Block block) {
        int r = 0, g = 0, b = 0;

        if (block.equals(Blocks.iron_ore)) {
            r = g = b = 255;
        } else if (block.equals(Blocks.gold_ore)) {
            r = g = 255;
        } else if (block.equals(Blocks.diamond_ore)) {
            g = 220;
            b = 255;
        } else if (block.equals(Blocks.emerald_ore)) {
            r = 35;
            g = 255;
        } else if (block.equals(Blocks.lapis_ore)) {
            g = 50;
            b = 255;
        } else if (block.equals(Blocks.redstone_ore)) {
            r = 255;
        } else if (block.equals(Blocks.mob_spawner)) {
            r = 30;
            b = 135;
        }

        return new int[]{r, g, b};
    }
}