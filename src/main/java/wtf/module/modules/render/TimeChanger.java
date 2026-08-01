package wtf.module.modules.render;

import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;
import wtf.event.EventLink;
import wtf.event.Listener;
import wtf.event.impl.PacketReceiveEvent;
import wtf.event.impl.PreTickEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;
import wtf.module.value.impl.NumberValue;
import wtf.util.Utils;

@ModuleInfo(name = "TimeChanger", category = Category.Render)
public class TimeChanger extends Mod {

	public final NumberValue time = new NumberValue("Time", this, 0, 0, 1, 0.01f);
	
	@Override
	public void onDisable() {
		super.onDisable();
		clear();
	}

    @EventLink
    private Listener<PreTickEvent> preTick = event -> {
		if (checkGame()) return;
		clear();
		getWorld().setWorldTime((long) (time.getValue() * 22999));
    };
	
    @EventLink
    private Listener<PacketReceiveEvent> packetReceive = event -> {
    	if (event.getPacket() instanceof S03PacketTimeUpdate) {
    		event.cancel();
		} else if (event.getPacket() instanceof S2BPacketChangeGameState) {
			S2BPacketChangeGameState wrapped = (S2BPacketChangeGameState) event.getPacket();
			if (wrapped.getGameState() == 1 || wrapped.getGameState() == 2) {
				event.cancel();
			}
		}
    };

	public void clear() {
		if (checkGame()) return;
		getWorld().setRainStrength(0);
		getWorld().getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
		getWorld().getWorldInfo().setRainTime(0);
		getWorld().getWorldInfo().setThunderTime(0);
		getWorld().getWorldInfo().setRaining(false);
		getWorld().getWorldInfo().setThundering(false);
	}
}
