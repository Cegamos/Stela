package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.PostRenderTickEvent;
import keystrokesmod.client.event.impl.PostTickEvent;
import keystrokesmod.client.event.impl.PreRenderTickEvent;
import keystrokesmod.client.event.impl.PreTickEvent;
import keystrokesmod.client.stela.annotations.Inject;
import keystrokesmod.client.stela.annotations.Mixin;
import keystrokesmod.client.stela.annotations.Shadow;
import keystrokesmod.client.stela.annotations.Target;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private Timer timer;

	@Inject(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "net/minecraftforge/fml/common/FMLCommonHandler.onRenderTickStart(F)V", shift = Target.Shift.BEFORE))
	private void injectSkipWorld() {
		EventBus.INSTANCE.post(new PreRenderTickEvent(this.timer.renderPartialTicks));
	}

	@Inject(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "net/minecraft/client/renderer/EntityRenderer.updateCameraAndRender(FJ)V", shift = Target.Shift.AFTER))
	private void injectPostRenderTick() {
		EventBus.INSTANCE.post(new PostRenderTickEvent(this.timer.renderPartialTicks));
	}

	@Inject(method = "runTick", desc = "()V", target = @Target("HEAD"))
	private void onRunTickPre() {
		EventBus.INSTANCE.post(new PreTickEvent());
	}

	@Inject(method = "runTick", desc = "()V", target = @Target("TAIL"))
	private void onRunTickPost() {
		EventBus.INSTANCE.post(new PostTickEvent());
	}

	@Inject(method = "startGame", desc = "()V", target = @Target("HEAD"))
	private void injectStartGame() {
		Raven.init();
	}
}
