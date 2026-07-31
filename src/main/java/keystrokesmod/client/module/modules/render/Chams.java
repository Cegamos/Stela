package keystrokesmod.client.module.modules.render;

import org.lwjgl.opengl.GL11;

import keystrokesmod.client.module.Category;
import keystrokesmod.client.module.ModuleInfo;
import keystrokesmod.client.module.modules.Mod;
import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@ModuleInfo(name = "Chams", category = Category.Render)
public class Chams extends Mod {
	@SubscribeEvent
	public void r1(final RenderPlayerEvent.Pre e) {
		if (e.entity != mc.thePlayer) {
			GL11.glEnable(32823);
			GL11.glPolygonOffset(1.0f, -1100000.0f);
		}
	}

	@SubscribeEvent
	public void r2(final RenderPlayerEvent.Post e) {
		if (e.entity != mc.thePlayer) {
			GL11.glDisable(32823);
			GL11.glPolygonOffset(1.0f, 1100000.0f);
		}
	}
}
