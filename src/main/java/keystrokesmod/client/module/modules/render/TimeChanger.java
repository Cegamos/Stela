package keystrokesmod.client.module.modules.render;

import keystrokesmod.client.event.EventLink;
import keystrokesmod.client.event.Listener;
import keystrokesmod.client.event.impl.PacketReceiveEvent;
import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.value.impl.NumberValue;
import keystrokesmod.client.util.Utils;
import net.minecraft.network.play.server.S03PacketTimeUpdate;
import net.minecraft.network.play.server.S2BPacketChangeGameState;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.common.gameevent.TickEvent;

@ModuleInfo(name = "TimeChanger", category = Category.Render)
public class TimeChanger extends Mod {

	public final NumberValue time = new NumberValue("Time", this, 0, 0, 1, 0.01f);
	
	@Override
	public void onDisable() {
		super.onDisable();
		clear();
	}
	
    @SubscribeEvent
    public void onTick(TickEvent event) {
		if (!Utils.Player.isPlayerInGame()) return;
		clear();
		mc.theWorld.setWorldTime((long) (time.getInput() * 22999));
    }
	
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
		if (!Utils.Player.isPlayerInGame()) return;
		mc.theWorld.setRainStrength(0);
		mc.theWorld.getWorldInfo().setCleanWeatherTime(Integer.MAX_VALUE);
		mc.theWorld.getWorldInfo().setRainTime(0);
		mc.theWorld.getWorldInfo().setThunderTime(0);
		mc.theWorld.getWorldInfo().setRaining(false);
		mc.theWorld.getWorldInfo().setThundering(false);
	}
}
