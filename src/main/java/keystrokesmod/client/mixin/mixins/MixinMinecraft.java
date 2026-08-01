package keystrokesmod.client.mixin.mixins;

import keystrokesmod.client.Raven;
import keystrokesmod.client.event.EventBus;
import keystrokesmod.client.event.impl.*;
import keystrokesmod.client.stela.annotations.*;
import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Timer;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private Timer timer;
	
	@Shadow
	public GameSettings gameSettings;
	
	@Shadow
	private static Minecraft theMinecraft;
	
	@Unique
	public static Timer staticTimer;

	@Unique
	public static Timer getStaticTimer() {
		if (staticTimer == null) {
			staticTimer = new Timer(20.0F);
		}
		return staticTimer;
	}
	
	@Shadow
	private void clickMouse() {}
	
	@Shadow
	private void rightClickMouse() {}
	
	@Shadow
	private void middleClickMouse() {}

	@Redirect(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "updateTimer"))
	public static void redirectUpdateTimer(Timer timerInstance) {
		Minecraft mc = Minecraft.getMinecraft();
		if (mc != null && mc.isGamePaused() && mc.theWorld != null) {
			if (timerInstance != null) {
				float f = timerInstance.renderPartialTicks;
				timerInstance.updateTimer();
				timerInstance.renderPartialTicks = f;
			}

			Timer sTimer = getStaticTimer();
			if (sTimer != null) {
				float f2 = sTimer.renderPartialTicks;
				sTimer.updateTimer();
				sTimer.renderPartialTicks = f2;
			}
		} else {
			if (timerInstance != null) {
				timerInstance.updateTimer();
			}
			Timer sTimer = getStaticTimer();
			if (sTimer != null) {
				sTimer.updateTimer();
			}
		}
	}

	@Inject(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "runTick", shift = Target.Shift.BEFORE))
	private void injectStaticTick() {
		Timer sTimer = getStaticTimer();
		for (int j = 0; j < sTimer.elapsedTicks; ++j) {
			EventBus.INSTANCE.post(new StaticTickEvent());
		}
	}

	@Inject(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "net/minecraftforge/fml/common/FMLCommonHandler.onRenderTickStart(F)V", shift = Target.Shift.BEFORE))
	private void injectSkipWorld() {
		EventBus.INSTANCE.post(new PreRenderTickEvent(this.timer.renderPartialTicks));
	}

	@Inject(method = "runGameLoop", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "net/minecraft/client/renderer/EntityRenderer.updateCameraAndRender(FJ)V", shift = Target.Shift.AFTER))
	private void injectPostRenderTick() {
		EventBus.INSTANCE.post(new PostRenderTickEvent(this.timer.renderPartialTicks));
	}
	
	@Inject(method = "runGameLoop", desc = "()V", target = @Target("HEAD"))
	public void injectPreRunGameLoop() {
		EventBus.INSTANCE.post(new GameEvent());
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

	@Inject(method = "runTick", desc = "()V", target = @Target(value = "INVOKEVIRTUAL", target = "isUsingItem", shift = Target.Shift.BEFORE))
	private void injectMouseStateUpdate() {
		MouseStateUpdateEvent mouseStateUpdateEvent = new MouseStateUpdateEvent();
		EventBus.INSTANCE.post(mouseStateUpdateEvent);

		if (theMinecraft.thePlayer != null) {
			if (theMinecraft.thePlayer.isUsingItem()) {
				if (!this.gameSettings.keyBindUseItem.isKeyDown()) {
					theMinecraft.playerController.onStoppedUsingItem(theMinecraft.thePlayer);
				}
				updateMouseState(mouseStateUpdateEvent, false);
			} else {
				updateMouseState(mouseStateUpdateEvent, false);
			}
		}
	}
	
	@Unique
	private void updateMouseState(MouseStateUpdateEvent event, boolean forceCancel) {
        if (event.isCancelled() || forceCancel) {
            while (this.gameSettings.keyBindAttack.isPressed()) {
            }

            while (this.gameSettings.keyBindUseItem.isPressed()) {
            }

            while (this.gameSettings.keyBindPickBlock.isPressed()) {
            }
        } else {
            while (this.gameSettings.keyBindAttack.isPressed()) {
                this.clickMouse();
            }

            while (this.gameSettings.keyBindUseItem.isPressed()) {
                this.rightClickMouse();
            }

            while (this.gameSettings.keyBindPickBlock.isPressed()) {
                this.middleClickMouse();
            }
        }
    }

}
