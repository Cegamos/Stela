package keystrokesmod.client.module.modules.player;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.Mod;
import keystrokesmod.client.module.ModuleInfo;
import net.minecraft.network.play.client.C03PacketPlayer;

@ModuleInfo(name = "NoFall", category = Category.Player)
public class NoFall extends Mod {
    
	@Override
    public void update() {
        if (mc.thePlayer.fallDistance > 2.5) {
            mc.thePlayer.sendQueue.addToSendQueue(new C03PacketPlayer(true));
        }
    }
}
