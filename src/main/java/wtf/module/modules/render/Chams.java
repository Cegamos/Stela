package wtf.module.modules.render;

import org.lwjgl.opengl.GL11;

import net.minecraftforge.client.event.RenderPlayerEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import wtf.module.Category;
import wtf.module.ModuleInfo;
import wtf.module.modules.Mod;

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
