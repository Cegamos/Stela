package keystrokesmod.client.module.modules.render;

import static org.lwjgl.opengl.GL11.GL_DEPTH_TEST;
import static org.lwjgl.opengl.GL11.GL_ONE_MINUS_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.GL_SRC_ALPHA;
import static org.lwjgl.opengl.GL11.glDisable;
import static org.lwjgl.opengl.GL11.glEnable;
import static org.lwjgl.opengl.GL11.glNormal3f;
import static org.lwjgl.opengl.GL11.glPopMatrix;
import static org.lwjgl.opengl.GL11.glPushMatrix;
import static org.lwjgl.opengl.GL11.glRotatef;
import static org.lwjgl.opengl.GL11.glScaled;
import static org.lwjgl.opengl.GL11.glTranslatef;

import java.awt.Color;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.timing.Clock;
import net.minecraft.block.Block;
import net.minecraft.block.BlockBed;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.util.BlockPos;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "BedPlates", category = Category.Render)
public class BedPlates extends Mod {
    private final List<BlockPos> beds = new ArrayList<>();
    private final List<List<Block>> bedBlocks = new ArrayList<>();
    private BlockPos[] bed = null;
    private Clock clock = new Clock(0);
    
    private final TickSetting firstBed = new TickSetting("Render First Bed", this, true);
    private final TickSetting showDistance = new TickSetting("Show Distance", this, true);
    private final SliderSetting range = new SliderSetting("Range", this, 10, 2, 30, 1);
    private final SliderSetting layers = new SliderSetting("Layers", this, 3, 1, 10, 1);
    public final SliderSetting plateScale = new SliderSetting("Scale", this, 1.0, 0.5, 3.0, 0.05);
    
    @Override
    public void update() {
        if (!Utils.Player.isPlayerInGame()) return;

        if (clock.hasFinished()) {
            clock.setCooldown(1000);
            clock.start();
        }

        final int rangeValue = (int) range.getInput();
        final double px = mc.thePlayer.posX;
        final double py = mc.thePlayer.posY;
        final double pz = mc.thePlayer.posZ;
        final World world = mc.theWorld;

        final Block bedBlock = Blocks.bed;
        final BlockBed.EnumPartType footPart = BlockBed.EnumPartType.FOOT;

        Set<BlockPos> bedsSet = new HashSet<>(beds);

        for (int y = rangeValue; y >= -rangeValue; --y) {
            for (int x = -rangeValue; x <= rangeValue; ++x) {
                for (int z = -rangeValue; z <= rangeValue; ++z) {
                    BlockPos pos = new BlockPos(px + x, py + y, pz + z);
                    IBlockState state = world.getBlockState(pos);

                    if (state.getBlock() != bedBlock) continue;
                    if (state.getValue(BlockBed.PART) != footPart) continue;

                    if (firstBed.isToggled()) {
                        if (bed != null && pos.equals(bed[0])) return;

                        bed = new BlockPos[]{
                            pos,
                            pos.offset(state.getValue(BlockBed.FACING))
                        };
                        return;
                    } else {
                        if (bedsSet.contains(pos)) continue;

                        beds.add(pos);
                        bedBlocks.add(new ArrayList<>());
                        bedsSet.add(pos);
                    }
                }
            }
        }
    }
    
    @Override
    public void onDisable() {
        this.beds.clear();
        this.bedBlocks.clear();
    }
    
	@SubscribeEvent
	public void onWorld(EntityJoinWorldEvent event) {
        if (event.entity == mc.thePlayer) {
            this.beds.clear();
            this.bedBlocks.clear();
            this.bed = null;
        }
    };

	@SubscribeEvent
	public void onRender3D(RenderWorldLastEvent event) {
        if (Utils.Player.isPlayerInGame()) {
            if (firstBed.isToggled() && this.bed != null) {
                if (!(mc.theWorld.getBlockState(bed[0]).getBlock() instanceof BlockBed)) {
                    this.bed = null;
                    return;
                }
                findBed(bed[0].getX(), bed[0].getY(), bed[0].getZ(), 0);
                this.drawPlate(bed[0], 0);
            }
            if (this.beds.isEmpty()) {
                return;
            }
            Iterator<BlockPos> iterator = this.beds.iterator();
            while (iterator.hasNext()) {
                BlockPos blockPos = iterator.next();
                if (!(mc.theWorld.getBlockState(blockPos).getBlock() instanceof BlockBed)) {
                    iterator.remove();
                    continue;
                }
                findBed(blockPos.getX(), blockPos.getY(), blockPos.getZ(), this.beds.indexOf(blockPos));
                this.drawPlate(blockPos, this.beds.indexOf(blockPos));
            }
        }
    }

	private void drawPlate(BlockPos blockPos, int index) {
	    float rotateX = mc.gameSettings.thirdPersonView == 2 ? -1.0F : 1.0F;
	    double distance = mc.thePlayer.getDistance(blockPos.getX(), blockPos.getY(), blockPos.getZ());
	    double scale = plateScale.getInput();
	    double base = 0.01666666753590107D * Math.sqrt(distance);

	    glPushMatrix();
	    glDisable(GL_DEPTH_TEST);
	    GlStateManager.enableBlend();
	    GlStateManager.blendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);

	    glTranslatef(
	        (float) (blockPos.getX() - mc.getRenderManager().viewerPosX + 0.5),
	        (float) (blockPos.getY() - mc.getRenderManager().viewerPosY + 2),
	        (float) (blockPos.getZ() - mc.getRenderManager().viewerPosZ + 0.5)
	    );
	    glNormal3f(0.0F, 1.0F, 0.0F);
	    glRotatef(-mc.getRenderManager().playerViewY, 0.0F, 1.0F, 0.0F);
	    glRotatef(mc.getRenderManager().playerViewX, rotateX, 0.0F, 0.0F);
	    glScaled(-base * scale, -base * scale, base * scale);

	    List<Block> blocks = bedBlocks.get(index);
	    int blockSize = (int) (17 * scale);
	    int iconSize = (int) (15 * scale);
	    int minWidth = blockSize;
	    int totalWidth = Math.max(minWidth, blocks.size() * blockSize);

	    int x1 = -totalWidth / 2;
	    int y1 = (int) (-2 * scale);
	    int x2 = x1 + totalWidth;
	    int y2 = (int) (26 * scale);

	    Gui.drawRect(x1, y1, x2, y2, new Color(0, 0, 0, 90).getRGB());

	    String dist = Math.round(distance) + "m";
        GlStateManager.scale(scale, scale, scale);
        mc.fontRendererObj.drawString(dist, -(int)(mc.fontRendererObj.getStringWidth(dist) / 2.0), 0, new Color(255, 255, 255, 255).getRGB());

	    double offset = (blocks.size() * (-17.5 * scale)) / 2;
	    for (Block block : blocks) {
	        mc.getTextureManager().bindTexture(getBlockTexture(block));
	        Gui.drawModalRectWithCustomSizedTexture((int) offset, (int)(10 * scale), 0, 0, iconSize, iconSize, iconSize, iconSize);
	        offset += 17.5 * scale;
	    }

	    GlStateManager.disableBlend();
	    glEnable(GL_DEPTH_TEST);
	    glPopMatrix();
	}


    private void findBed(double x, double y, double z, int index) {
        BlockPos bedPos = new BlockPos(x, y, z);
        Block bedBlock = mc.theWorld.getBlockState(bedPos).getBlock();

        while (bedBlocks.size() <= index) {
            bedBlocks.add(new ArrayList<>());
        }
        
        bedBlocks.get(index).clear();

        while (beds.size() <= index) {
            beds.add(null);
        }
        
        beds.set(index, null);

        if (beds.contains(bedPos) || !bedBlock.equals(Blocks.bed)) {
            return;
        }

        bedBlocks.get(index).add(Blocks.bed);
        beds.set(index, bedPos);

        int[][] directions = {
                {0, 1, 0},  // Arriba
                {1, 0, 0},  // Derecha
                {-1, 0, 0}, // Izquierda
                {0, 0, 1},  // Frente
                {0, 0, -1}  // Atrás
        };

        int layersCount = (int) layers.getInput();

        for (int[] dir : directions) {
            for (int layer = 1; layer <= layersCount; layer++) {
                BlockPos currentPos = bedPos.add(dir[0] * layer, dir[1] * layer, dir[2] * layer);
                Block currentBlock = mc.theWorld.getBlockState(currentPos).getBlock();

                if (currentBlock.equals(Blocks.air)) {
                    break;
                }

                if (isValidBedBlock(currentBlock) && !bedBlocks.get(index).contains(currentBlock)) {
                    bedBlocks.get(index).add(currentBlock);
                }
            }
        }
    }

    private boolean isValidBedBlock(Block block) {
        return block.equals(Blocks.wool) || block.equals(Blocks.stained_hardened_clay) ||
                block.equals(Blocks.stained_glass) || block.equals(Blocks.glass) || block.equals(Blocks.planks) ||
                block.equals(Blocks.log) || block.equals(Blocks.log2) ||
                block.equals(Blocks.end_stone) || block.equals(Blocks.obsidian) ||
                block.equals(Blocks.water) || block.equals(Blocks.ladder);
    }

    public static boolean isSamePos(BlockPos blockPos, BlockPos blockPos2) {
        return blockPos == blockPos2 || (blockPos.getX() == blockPos2.getX() && blockPos.getY() == blockPos2.getY() && blockPos.getZ() == blockPos2.getZ());
    }

    public ResourceLocation getBlockTexture(Block block) {
        if (block == Blocks.bed) {
            return new ResourceLocation("minecraft:textures/items/bed.png");
        } else if (block == Blocks.obsidian) {
            return new ResourceLocation("minecraft:textures/blocks/obsidian.png");
        } else if (block == Blocks.end_stone) {
            return new ResourceLocation("minecraft:textures/blocks/end_stone.png");
        } else if (block == Blocks.stained_hardened_clay) {
            return new ResourceLocation("minecraft:textures/blocks/hardened_clay_stained_white.png");
        } else if (block == Blocks.stained_glass) {
            return new ResourceLocation("minecraft:textures/blocks/glass.png");
        } else if (block == Blocks.water) {
            return new ResourceLocation("minecraft:textures/blocks/water_still.png");
        } else if (block == Blocks.planks) {
            return new ResourceLocation("minecraft:textures/blocks/planks_oak.png");
        } else if (block == Blocks.wool) {
            return new ResourceLocation("minecraft:textures/blocks/wool_colored_white.png");
        } else {
            return new ResourceLocation("minecraft:textures/blocks/stone.png");
        }
    }

}
