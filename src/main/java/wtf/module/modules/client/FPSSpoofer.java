package wtf.module.modules.client;

import java.util.concurrent.ThreadLocalRandom;

import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PreTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.DescriptionValue;
import wtf.module.value.impl.RangeValue;
import wtf.util.system.ReflectUtil;

@ModuleInfo(name = "FPSSpoofer", category = Category.Client)
public class FPSSpoofer extends Mod {
	protected final DescriptionValue desc = new DescriptionValue("Spoofs your fps", this);
    private final RangeValue fps = new RangeValue("FPS", this, 99860, 100000, 0, 100000, 100);

    protected int ticksPassed;
    
    @Override
    public void onEnable() {
    	super.onEnable();
        this.ticksPassed = 0;
    }

    @EventLink
    public final Listener<PreTickEvent> onTick = event -> {
        this.guiUpdate();
        int fakeFps = ThreadLocalRandom.current().nextInt(
                (int) fps.getInputMin(),
                (int) fps.getInputMax() + 1
            );
        ReflectUtil.setFpsCounter(fakeFps);
        this.ticksPassed = 0;
        ++this.ticksPassed;
    };
}