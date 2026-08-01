package wtf.mixin.mixins;

import net.minecraft.client.Minecraft;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.util.Timer;
import wtf.Kevin;
import wtf.event.EventBus;
import wtf.event.impl.*;
import wtf.stela.annotations.*;
import wtf.util.font.FontUtil;

@Mixin(Minecraft.class)
public class MixinMinecraft {
	@Shadow
	private Timer timer;
	
	@Shadow
	public GameSettings gameSettings;
	
	@Shadow
	private static Minecraft theMinecraft;

	@Shadow
	private void clickMouse() {}
	
	@Shadow
	private void rightClickMouse() {}
	
	@Shadow
	private void middleClickMouse() {}


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

	@Inject(method = "startGame", desc = "()V", target = @Target("TAIL"))
	private void injectStartGame() {
		Kevin.init();
		FontUtil.checkInit();
	}

	@Inject(method = "shutdownMinecraftApplet", desc = "()V", target = @Target("HEAD"))
	private void injectShutdown() {
		Kevin.shutdown();
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
