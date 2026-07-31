package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.ReflectUtil;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "DelayRemover", category = Category.Combat)
public class DelayRemover extends Mod {
	private final DescriptionValue desc = new DescriptionValue("Remove click and jump delay", this);

    public BooleanValue jump = new BooleanValue("Jump", this, false);
    public BooleanValue click = new BooleanValue("1.7 HitReg", this, true);
    
    @SubscribeEvent
    public void playerTickEvent(final TickEvent.PlayerTickEvent event) {
        if (Utils.Player.isPlayerInGame()) {
            if (!mc.inGameHasFocus) {
                return;
            }
            
            if (jump.isToggled()) {
            	ReflectUtil.setJumpTicks(0);
            }
            
            if (click.isToggled()) {
            	ReflectUtil.setLeftClickCounter(0);
            }
        }
    }
}
