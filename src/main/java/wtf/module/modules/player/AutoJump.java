package wtf.module.modules.player;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PostPlayerTickEvent;
import wtf.event.impl.PrePlayerTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.BooleanValue;
import wtf.util.Utils;

@ModuleInfo(name = "AutoJump", category = Category.Player)
public class AutoJump extends Mod {
    private final BooleanValue b = new BooleanValue("Cancel when shifting", this, true);
    private boolean edge = false;
    
    @Override
    public void onDisable() {
        super.onDisable();
        legitJump(false);
        this.edge = false;
    }
    
    @EventLink
    private Listener<PrePlayerTickEvent> prePlayerTick = event -> both();
    
    @EventLink
    private Listener<PostPlayerTickEvent> postPlayerTick = event -> both();
    
    private void both() {
        if (Utils.Player.isPlayerInGame()) {
            if (ground() && (!b.getValue() || !sneaking())) {
                boolean overGap = getWorld().getCollidingBoundingBoxes(getPlayer(), getPlayer().getEntityBoundingBox().offset(motionX() / 3.0, -1.0, motionZ() / 3.0)).isEmpty();
                
                if (!this.edge && overGap) {
                    legitJump(true);
                    this.edge = true;
                } else if (this.edge && !overGap) {
                    legitJump(false);
                    this.edge = false;
                }
            } else if (this.edge) {
                legitJump(false);
                this.edge = false;
            }
        }
    }
}