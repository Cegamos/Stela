package keystrokesmod.client.module.modules.combat;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PostPlayerTickEvent;
import keystrokesmod.client.event.impl.PrePlayerTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import keystrokesmod.client.module.value.impl.BooleanValue;
import keystrokesmod.client.module.value.impl.DescriptionValue;
import keystrokesmod.client.util.Utils;
import keystrokesmod.client.util.system.ReflectUtil;

@ModuleInfo(name = "DelayRemover", category = Category.Combat)
public class DelayRemover extends Mod {
	private final DescriptionValue desc = new DescriptionValue("Remove click and jump delay", this);

    public BooleanValue jump = new BooleanValue("Jump", this, false);
    public BooleanValue click = new BooleanValue("1.7 HitReg", this, true);
    
    @EventLink
    private Listener<PrePlayerTickEvent> prePlayerTick = event -> both();
    
    @EventLink
    private Listener<PostPlayerTickEvent> postPlayerTick = event -> both();
    
    private void both() {
        if (Utils.Player.isPlayerInGame()) {
            if (!mc.inGameHasFocus) return;
            
            if (jump.getValue()) {
            	ReflectUtil.setJumpTicks(0);
            }
            
            if (click.getValue()) {
            	ReflectUtil.setLeftClickCounter(0);
            }
        }
    }
}
