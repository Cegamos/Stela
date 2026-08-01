package wtf.module.modules.render;

import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.DescriptionValue;
import wtf.util.Utils;

@ModuleInfo(name = "Fullbright", category = Category.Render)
public class Fullbright extends Mod {
	private final DescriptionValue desc = new DescriptionValue("No more darkness!", this);
    private float defaultGamma;
    private final float clientGamma = 10000.0f;
    
    @Override
    public void onEnable() {
        this.defaultGamma = mc.gameSettings.gammaSetting;
        super.onEnable();
    }
    
    @Override
    public void onDisable() {
        super.onEnable();
        mc.gameSettings.gammaSetting = this.defaultGamma;
    }
    
    @SubscribeEvent
    public void onPlayerTick(final TickEvent.PlayerTickEvent e) {
        if (!Utils.Player.isPlayerInGame()) {
            this.onDisable();
            return;
        }
        
        if (mc.gameSettings.gammaSetting != this.clientGamma) {
            mc.gameSettings.gammaSetting = this.clientGamma;
        }
    }
}
