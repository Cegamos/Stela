package keystrokesmod.client.module.modules.client;

import java.util.concurrent.ThreadLocalRandom;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.setting.impl.DescriptionSetting;
import keystrokesmod.client.module.setting.impl.DoubleSliderSetting;
import keystrokesmod.client.utils.system.ReflectUtil;

@ModuleInfo(name = "FPSSpoofer", category = Category.Client)
public class FPSSpoofer extends Mod {
	private final DescriptionSetting desc = new DescriptionSetting("Spoofs your fps", this);
    private final DoubleSliderSetting fps = new DoubleSliderSetting("FPS", this, 99860, 100000, 0, 100000, 100);

    private int ticksPassed;
    
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