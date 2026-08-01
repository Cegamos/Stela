package wtf.module.modules.combat;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PostPlayerTickEvent;
import wtf.event.impl.PrePlayerTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.module.value.impl.DescriptionValue;
import wtf.util.Utils;
import wtf.util.system.ReflectUtil;

@ModuleInfo(name = "DelayRemover", category = Category.Combat)
public class DelayRemover extends Mod {
	protected final DescriptionValue desc = new DescriptionValue("Remove click and jump delay", this);

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
