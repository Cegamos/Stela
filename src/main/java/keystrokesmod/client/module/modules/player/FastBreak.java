package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.ComboSetting;
import keystrokesmod.client.module.setting.impl.SliderSetting;
import keystrokesmod.client.module.setting.impl.TickSetting;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraft.block.Block;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "FastBreak", category = Category.Player)
public class FastBreak extends Mod {
	
	private final ComboSetting mode = new ComboSetting("Mode", this, "Normal", "Normal", "Ticks", "Instant");
    private final SliderSetting speed = new SliderSetting("Speed", this, 50, 0, 100, 1, () -> mode.is("Normal"));
    private final SliderSetting ticks = new SliderSetting("Ticks", this, 1, 1, 50, 1, () -> mode.is("Ticks"));
    private final TickSetting ignoringMiningFatigue = new TickSetting("Ignore Mining Fatigue", this, false);

    @SubscribeEvent
    public void onPlayerTick(final TickEvent.PlayerTickEvent event) {
        if (event.phase == TickEvent.Phase.START) {
        	if (mc.thePlayer == null || mc.playerController == null) return;

            if (ignoringMiningFatigue.isToggled()) {
                mc.thePlayer.removePotionEffect(Potion.digSlowdown.getId());
            }

            ReflectUtil.setBlockHitDelay(0);

            double faster = 0;

            switch (mode.getMode()) {
                case "Normal":
                    faster = speed.getInput() / 100.0;
                    break;

                case "Instant":
                	ReflectUtil.setCurBlockDamage(1f);
                    break;

                case "Ticks":
                    if (mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
                        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
                        Block block = block(blockPos);

                        if (block != null) {
                            float blockHardness = block.getPlayerRelativeBlockHardness(mc.thePlayer, mc.theWorld, blockPos);
                            if (blockHardness > 0) {
                                faster = blockHardness * ticks.getInput();
                            }
                        }
                    }
                    break;
            }
            
            if (ReflectUtil.getCurBlockDamage() > 1.0 - faster && ReflectUtil.getCurBlockDamage() < 0.99f) {
            	ReflectUtil.setCurBlockDamage(0.99f);
            }
        }
    }
    
	public Block block(final BlockPos blockPos) {
		return mc.theWorld.getBlockState(blockPos).getBlock();
	}
}